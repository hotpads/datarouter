package net.spy.memcached;
 
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import net.spy.SpyObject;

import com.google.common.collect.Lists;
import com.hotpads.datarouter.util.core.GenericsFactory;
 
/**
* This is an implementation of the Ketama consistent hash strategy from
* last.fm. This implementation may not be compatible with libketama as
* hashing is considered separate from node location.
*
* Note that this implementation does not currently supported weighted nodes.
*
* @see http://www.last.fm/user/RJ/journal/2007/04/10/392555/
*/
public final class KetamaNodeLocator extends SpyObject implements NodeLocator {
 
    static final int NUM_REPS = 160;
 
    TreeMap<Long, ServerInfo> ketamaNodes;
    Collection<MemcachedNode> allNodes;
    final List<ServerInfo> goodServers;
    final List<ServerInfo> failedServers;
 
    final HashAlgorithm hashAlg;
 
    public KetamaNodeLocator(ServerInfo[] serverList, HashAlgorithm alg) {
        super();
        goodServers = Lists.newArrayList(serverList);
        failedServers = Lists.newArrayList();
        hashAlg = alg;
        createContinuum();
    }
    
    public static long computeHash(String key, int alignment) {
        byte[] digest = HashAlgorithm.computeMd5(key);
        long result = ((long) (digest[3 + alignment * 4] & 0xFF) << 24)
            | ((long) (digest[2 + alignment * 4] & 0xFF) << 16)
            | ((long) (digest[1 + alignment * 4] & 0xFF) << 8)
            | (digest[alignment * 4] & 0xFF);
        return result;
    }
    
    private void createContinuum() {
        // we use (NUM_REPS * #servers) total points, but allocate them based on server weights.
        double total_weight = 0.0;
        for (ServerInfo sinfo : goodServers) {
            total_weight += sinfo.weight;
        }
        int total_servers = goodServers.size();
        int nodes_per_server = (hashAlg == HashAlgorithm.KETAMA_HASH) ? NUM_REPS / 4 : NUM_REPS;
 
        allNodes = new ArrayList<MemcachedNode>();
        ketamaNodes = new TreeMap<Long, ServerInfo>();
 
        for (ServerInfo sinfo : goodServers) {
            double percent = (double) sinfo.weight / total_weight;
            // the tiny fudge fraction is added to counteract float errors.
            int item_weight = (int) (percent * total_servers * nodes_per_server + 0.0000000001);
            for (int k = 0; k < item_weight; k++) {
                String key = (sinfo.port == 11211) ? (sinfo.hostname + "-" + k) : (sinfo.hostname + ":" + sinfo.port + "-" + k);
                if (hashAlg == HashAlgorithm.KETAMA_HASH) {
                    for (int i = 0; i < 4; i++) {
                        ketamaNodes.put(computeHash(key, i), sinfo);
                    }
                } else {
                    ketamaNodes.put(hashAlg.hash(key), sinfo);
                }
            }
            allNodes.add(sinfo.connection);
        }
        
        // because of int flooring, we may have fewer nodes than (NUM_REPS * total_servers), but it will be bounded.
        assert ketamaNodes.size() <= NUM_REPS * total_servers;
        assert ketamaNodes.size() >= NUM_REPS * (total_servers - 1);
    }
    
    @Override
    public String toString() {
        StringBuilder dump = new StringBuilder("<KetamaNodeLocator");
        dump.append(" nodes=" + ketamaNodes.size());
        dump.append(" all nodes=" + allNodes.size());
        dump.append(" good servers=" + goodServers.size());
        dump.append(" failed servers=" + failedServers.size());
        dump.append(">");
        return dump.toString();
    }
    
    public Collection<MemcachedNode> getAll() {
        return allNodes;
    }
    
    public List<ServerInfo> getGoodServers() {
        return goodServers;
    }
    
    public List<ServerInfo> getFailedServers() {
    	return failedServers;
    }
    
    public void removeServer(MemcachedNode node) {
    	ServerInfo dead = null;
    	for (ServerInfo server : goodServers)
    		if (server.connection.equals(node))
    			dead = server;
    	if (dead != null) {
    		goodServers.remove(dead);
    		failedServers.add(dead);
        	createContinuum();
    	} else {
    		getLogger().error("couldnt find server to remove for " + node.toString());
    	}
    }
    
    public void addServer(ServerInfo server) {
    	goodServers.add(server);
    	createContinuum();
    }
      
    public void moveAllFailedToGood() {
    	List<ServerInfo> temp = GenericsFactory.makeArrayList();
    	temp.addAll(failedServers);
    	failedServers.clear();
    	goodServers.addAll(temp);
    	createContinuum();
    }
    
    public MemcachedNode getPrimary(final String k) {
        ServerInfo rv = getNodeForKey(hashAlg.hash(k));
        if (rv == null)
        	return null;
        return rv.connection;
    }
 
    long getMaxKey() {
        return ketamaNodes.lastKey();
    }
 
    ServerInfo getNodeForKey(long hash) {
    	try {
	        if(!ketamaNodes.containsKey(hash)) {
	            // Java 1.6 adds a ceilingKey method, but I'm still stuck in 1.5
	            // in a lot of places, so I'm doing this myself.
	            SortedMap<Long, ServerInfo> tailMap=ketamaNodes.tailMap(hash);
	            if(tailMap.isEmpty()) {
	                hash=ketamaNodes.firstKey();
	            } else {
	                hash=tailMap.firstKey();
	            }
	        }
	        return ketamaNodes.get(hash);
    	} catch (NoSuchElementException e) {
    		return null; //most likely, all memcached servers down
    	}
    }
 
    public Iterator<MemcachedNode> getSequence(String k) {
        return new KetamaIterator(k, allNodes.size());
    }
 
 
    class KetamaIterator implements Iterator<MemcachedNode> {
 
        final String key;
        long hashVal;
        int remainingTries;
        int numTries=0;
 
        public KetamaIterator(final String k, final int t) {
            super();
            hashVal=hashAlg.hash(k);
            remainingTries=t;
            key=k;
        }
 
        private void nextHash() {
            // this.calculateHash(Integer.toString(tries)+key).hashCode();
            long tmpKey=hashAlg.hash((numTries++) + key);
            // This echos the implementation of Long.hashCode()
            hashVal += (int)(tmpKey ^ (tmpKey >>> 32));
            hashVal &= 0xffffffffL; /* truncate to 32-bits */
            remainingTries--;
        }
 
        public boolean hasNext() {
            return remainingTries > 0;
        }
 
        public MemcachedNode next() {
            try {
                return getNodeForKey(hashVal).connection;
            } finally {
                nextHash();
            }
        }
 
        public void remove() {
            throw new UnsupportedOperationException("remove not supported");
        }
 
    }
}