package com.hotpads.datarouter.client.imp.hbase.balancer.imp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.apache.hadoop.hbase.ServerName;

import com.hotpads.datarouter.client.imp.hbase.balancer.BalanceLeveler;
import com.hotpads.datarouter.client.imp.hbase.balancer.BaseHBaseRegionBalancer;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHRegionInfo;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.datarouter.util.core.DrByteTool;
import com.hotpads.datarouter.util.core.DrMapTool;
import com.hotpads.util.core.bytes.ByteRange;

/*
 * assign each partition in full to a server, and hard-level the number of regions
 */
public class ScatteringPrefixBalancer
extends BaseHBaseRegionBalancer{
	
	private Map<ByteRange,List<DRHRegionInfo<?>>> regionsByPrefix;
	
	/******************* constructor ***************************/
	
	public ScatteringPrefixBalancer(){
	}
	
	@Override
	public SortedMap<DRHRegionInfo<?>,ServerName> call(){
		initRegionByPrefixMap();
		groupRegionsByPrefix();
		
		//set up the ring of servers
		SortedMap<Long,ServerName> consistentHashRing = ConsistentHashBalancer.buildServerHashRing(drhServerList, 
				ConsistentHashBalancer.BUCKETS_PER_NODE);
		
		//calculate each prefix's position in the ring and store it
		SortedMap<ByteRange,ServerName> serverByPrefix = DrMapTool.createTreeMap();
		for(ByteRange prefix : regionsByPrefix.keySet()){
			byte[] consistentHashInput = prefix.copyToNewArray();
			ServerName serverName = ConsistentHashBalancer.calcServerNameForItem(consistentHashRing, consistentHashInput);
			serverByPrefix.put(prefix, serverName);//now region->server mapping is known
		}
		
		//level out any imbalances from the hashing
		BalanceLeveler<ByteRange,ServerName> leveler = new BalanceLeveler<ByteRange,ServerName>(
				drhServerList.getServerNames(), serverByPrefix);
		serverByPrefix = leveler.getBalancedDestinationByItem();

		//map individual regions to servers based on their prefix
		for(Map.Entry<ByteRange,ServerName> entry : serverByPrefix.entrySet()){
			List<DRHRegionInfo<?>> regionsInPrefix = regionsByPrefix.get(entry.getKey());
			for(DRHRegionInfo<?> region : regionsInPrefix){
				serverByRegion.put(region, entry.getValue());
			}
		}
//		logger.warn(getServerByRegionStringForDebug());
		assertRegionCountsConsistent();
		return serverByRegion;
	}
	
	
	private void initRegionByPrefixMap(){
		regionsByPrefix = DrMapTool.createTreeMap();
		for(List<Field<?>> prefixFields : scatteringPrefix.getAllPossibleScatteringPrefixes()){
			ByteRange prefix = new ByteRange(FieldSetTool.getConcatenatedValueBytes(prefixFields, false, false));
			regionsByPrefix.put(prefix, new ArrayList<DRHRegionInfo<?>>()); 
		}
	}
	
	
	private void groupRegionsByPrefix(){
		//group the regions by prefix
		for(DRHRegionInfo<?> drhRegionInfo : drhRegionList.getRegionsSorted()){
			byte[] startKey = drhRegionInfo.getRegion().getStartKey();
			if(DrArrayTool.isEmpty(startKey)){//use first prefix (is there a more robust way?)
				List<Field<?>> firstPrefix = scatteringPrefix.getAllPossibleScatteringPrefixes().get(0);
				startKey = FieldSetTool.getConcatenatedValueBytes(firstPrefix, false, false);
			}
			ByteRange regionPrefixBytes = new ByteRange(DrByteTool.copyOfRange(startKey, 0, scatteringPrefix
					.getNumPrefixBytes()));
			regionsByPrefix.get(regionPrefixBytes).add(drhRegionInfo);
		}
	}
	
}
