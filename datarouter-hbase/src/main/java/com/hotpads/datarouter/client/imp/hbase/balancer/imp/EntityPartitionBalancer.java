package com.hotpads.datarouter.client.imp.hbase.balancer.imp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.hadoop.hbase.ServerName;

import com.hotpads.datarouter.client.imp.hbase.balancer.BalanceLeveler;
import com.hotpads.datarouter.client.imp.hbase.balancer.BaseHBaseRegionBalancer;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHRegionInfo;

/*
 * assign each partition in full to a server, and hard-level the number of regions
 */
public class EntityPartitionBalancer
extends BaseHBaseRegionBalancer{
	
	private final String tableName;
	private Map<Integer,List<DRHRegionInfo<?>>> regionsByPartition;
	
	/******************* constructor ***************************/
	
	public EntityPartitionBalancer(String tableName){
		this.tableName = tableName;
	}
	
	@Override
	public SortedMap<DRHRegionInfo<?>,ServerName> call(){
		initRegionByPartitionMap();
		
		//set up the ring of servers
		SortedMap<Long,ServerName> consistentHashRing = ConsistentHashBalancer.buildServerHashRing(drhServerList, 
				ConsistentHashBalancer.BUCKETS_PER_NODE);
		
		//calculate each partition's position in the ring and store it
		SortedMap<Integer,ServerName> serverByPartition = new TreeMap<>();
		for(Integer partition : regionsByPartition.keySet()){
			byte[] consistentHashInput = entityPartitioner.getPrefix(partition);
			ServerName serverName = ConsistentHashBalancer.calcServerNameForItem(consistentHashRing,
					consistentHashInput);
			serverByPartition.put(partition, serverName);//now region->server mapping is known
		}
		
		//level out any imbalances from the hashing
		BalanceLeveler<Integer,ServerName> leveler = new BalanceLeveler<Integer,ServerName>(
				drhServerList.getServerNames(), serverByPartition, tableName);
		serverByPartition = leveler.getBalancedDestinationByItem();

		//map individual regions to servers based on their prefix
		for(Map.Entry<Integer,ServerName> entry : serverByPartition.entrySet()){
			List<DRHRegionInfo<?>> regionsInPartition = regionsByPartition.get(entry.getKey());
			for(DRHRegionInfo<?> region : regionsInPartition){
				serverByRegion.put(region, entry.getValue());
			}
		}
//		logger.warn(getServerByRegionStringForDebug());
		assertRegionCountsConsistent();
		return serverByRegion;
	}
	
	
	private void initRegionByPartitionMap(){
		regionsByPartition = new TreeMap<>();
		for(Integer partition : entityPartitioner.getAllPartitions()){
			regionsByPartition.put(partition, new ArrayList<DRHRegionInfo<?>>()); 
		}
		for(DRHRegionInfo<?> drhRegionInfo : drhRegionList.getRegionsSorted()){
			Integer partition = drhRegionInfo.getPartition();
			if(partition == null) {
				partition = 0;
			}
			regionsByPartition.get(partition).add(drhRegionInfo);
		}
	}
	
	
}
