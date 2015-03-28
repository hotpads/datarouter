package net.spy.memcached;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.hotpads.util.core.exception.NotImplementedException;

/**
 * NodeLocator implementation for dealing with simple array lookups using a
 * modulus of the hash code and node list length.
 */
public final class ArrayModNodeLocator implements NodeLocator {

	final List<MemcachedNode> nodes;

	private final HashAlgorithm hashAlg;

	/**
	 * Construct an ArraymodNodeLocator over the given array of nodes and
	 * using the given hash algorithm.
	 *
	 * @param n the array of nodes
	 * @param alg the hash algorithm
	 */
	  public ArrayModNodeLocator(ServerInfo[] serverList, HashAlgorithm alg) {
	    super();
	    nodes = new ArrayList<MemcachedNode>(serverList.length);
	    for (int i = 0; i < serverList.length; i++) {
	    	nodes.add(serverList[i].connection);
	    }
	    hashAlg=alg;
	  }

	public Collection<MemcachedNode> getAll() {
		return nodes;
	}
	
	public void removeServer(MemcachedNode node) {
		nodes.remove(node);
	}

	public MemcachedNode getPrimary(String k) {
		return nodes.get(getServerForKey(k));
	}

	public Iterator<MemcachedNode> getSequence(String k) {
		return new NodeIterator(getServerForKey(k));
	}

	private int getServerForKey(String key) {
		int rv=(int)(hashAlg.hash(key) % nodes.size());
		assert rv >= 0 : "Returned negative key for key " + key;
		assert rv < nodes.size()
			: "Invalid server number " + rv + " for key " + key;
		return rv;
	}


	class NodeIterator implements Iterator<MemcachedNode> {

		private final int start;
		private int next=0;

		public NodeIterator(int keyStart) {
			start=keyStart;
			next=start;
			computeNext();
			assert next >= 0 || nodes.size() == 1
				: "Starting sequence at " + start + " of "
					+ nodes.size() + " next is " + next;
		}

		public boolean hasNext() {
			return next >= 0;
		}

		private void computeNext() {
			if(++next >= nodes.size()) {
				next=0;
			}
			if(next == start) {
				next=-1;
			}
		}

		public MemcachedNode next() {
			try {
				return nodes.get(next);
			} finally {
				computeNext();
			}
		}

		public void remove() {
			throw new UnsupportedOperationException("Can't remove a node");
		}

	}


	@Override
	public void addServer(ServerInfo server) {
		throw new NotImplementedException("not implemented for arraymod locator");
	}

	@Override
	public Collection<ServerInfo> getFailedServers() {
		throw new NotImplementedException("not implemented for arraymod locator");	
	}

	@Override
	public void moveAllFailedToGood() {
		throw new NotImplementedException("not implemented for arraymod locator");
	}
}
