package com.hotpads.datarouter.client.imp.hbase.balancer.imp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.apache.hadoop.hbase.ServerName;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.imp.hbase.balancer.BalanceLeveler;
import com.hotpads.datarouter.client.imp.hbase.balancer.BalancerStrategy;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHRegionInfo;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHRegionList;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHServerList;
import com.hotpads.datarouter.storage.prefix.ScatteringPrefix;
import com.hotpads.util.core.ByteTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.bytes.ByteRange;
import com.hotpads.util.core.java.ReflectionTool;

/*
 * assign each partition in full to a server, and hard-level the number of regions
 */
public class ScatteringPrefixBalancer
implements BalancerStrategy{
	private static Logger logger = Logger.getLogger(ScatteringPrefixBalancer.class);
	
	protected SortedMap<DRHRegionInfo<?>,ServerName> serverByRegion;
	protected Class<? extends ScatteringPrefix> scatteringPrefixClass;
	protected ScatteringPrefix scatteringPrefix;
	
	/******************* constructor ***************************/
	
	public ScatteringPrefixBalancer(Class<? extends ScatteringPrefix> scatteringPrefixClass){//public no-arg for reflection
		this.serverByRegion = MapTool.createTreeMap();
		this.scatteringPrefixClass = scatteringPrefixClass;
		this.scatteringPrefix = ReflectionTool.create(scatteringPrefixClass);
	}
	
	@Override
	public ScatteringPrefixBalancer initMappings(DRHServerList drhServerList, DRHRegionList drhRegionList){
		//group the regions by prefix
		Map<ByteRange,List<DRHRegionInfo<?>>> regionsByPrefix = MapTool.createTreeMap();
		for(DRHRegionInfo<?> drhRegionInfo : drhRegionList.getRegionsSorted()){
			ByteRange regionPrefixBytes = new ByteRange(ByteTool.copyOfRange(drhRegionInfo.getRegion().getStartKey(), 0, 
					1));
			if(regionsByPrefix.get(regionPrefixBytes)==null){ 
				regionsByPrefix.put(regionPrefixBytes, new ArrayList<DRHRegionInfo<?>>()); 
			}
			regionsByPrefix.get(regionPrefixBytes).add(drhRegionInfo);
		}
		
		//set up the ring of servers
		SortedMap<Long,ServerName> consistentHashRing = ConsistentHashBalancer.buildServerHashRing(drhServerList, 
				ConsistentHashBalancer.BUCKETS_PER_NODE);
		
		//calculate each prefix's position in the ring and store it
		SortedMap<ByteRange,ServerName> serverByPrefix = MapTool.createTreeMap();
		for(ByteRange prefix : regionsByPrefix.keySet()){
			byte[] consistentHashInput = prefix.copyToNewArray();
			ServerName serverName = ConsistentHashBalancer.calcServerNameForItem(consistentHashRing, consistentHashInput);
			serverByPrefix.put(prefix, serverName);//now region->server mapping is known
		}
		
		//level out any imbalances from the hashing
		BalanceLeveler<ByteRange,ServerName> leveler = new BalanceLeveler<ByteRange,ServerName>(
				serverByPrefix);
		serverByPrefix = leveler.getBalancedDestinationByItem();

		//map individual regions to servers based on their prefix
		for(Map.Entry<ByteRange,ServerName> entry : serverByPrefix.entrySet()){
			List<DRHRegionInfo<?>> regionsInPrefix = regionsByPrefix.get(entry.getKey());
			for(DRHRegionInfo<?> region : regionsInPrefix){
				serverByRegion.put(region, entry.getValue());
			}
		}
		logger.warn(getServerByRegionStringForDebug());
		return this;
	}
	
	
	@Override
	public ServerName getServerName(DRHRegionInfo<?> drhRegionInfo) {
		return serverByRegion.get(drhRegionInfo);
	}
	
	private String getServerByRegionStringForDebug(){
		int i = 0;
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<DRHRegionInfo<?>,ServerName> entry : serverByRegion.entrySet()){
			sb.append(StringTool.pad(i+"", ' ', 3)
					+entry.getKey().getRegion().getEncodedName()
					+", "+entry.getValue());
		}
		return sb.toString();
	}
	
}
