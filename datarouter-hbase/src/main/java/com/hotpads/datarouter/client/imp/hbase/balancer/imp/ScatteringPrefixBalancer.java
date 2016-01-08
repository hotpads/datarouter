package com.hotpads.datarouter.client.imp.hbase.balancer.imp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.hadoop.hbase.ServerName;

import com.hotpads.datarouter.client.imp.hbase.balancer.BaseHBaseRegionBalancer;
import com.hotpads.datarouter.client.imp.hbase.balancer.HBaseBalanceLeveler;
import com.hotpads.datarouter.client.imp.hbase.cluster.DrRegionInfo;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.datarouter.util.core.DrByteTool;
import com.hotpads.util.core.bytes.ByteRange;

/*
 * assign each partition in full to a server, and hard-level the number of regions
 */
public class ScatteringPrefixBalancer
extends BaseHBaseRegionBalancer{
	
	private Map<ByteRange,List<DrRegionInfo<?>>> regionsByPrefix;
	
	/******************* constructor ***************************/
	
	public ScatteringPrefixBalancer(String tableName){
		super(tableName);
	}
	
	@Override
	public SortedMap<DrRegionInfo<?>,ServerName> call(){
		initRegionByPrefixMap();
		groupRegionsByPrefix();
		
		//set up the ring of servers
		SortedMap<Long,ServerName> consistentHashRing = ConsistentHashBalancer.buildServerHashRing(drhServerList, 
				ConsistentHashBalancer.BUCKETS_PER_NODE);
		
		//calculate each prefix's position in the ring and store it
		SortedMap<ByteRange,ServerName> serverByPrefix = new TreeMap<>();
		for(ByteRange prefix : regionsByPrefix.keySet()){
			byte[] consistentHashInput = prefix.copyToNewArray();
			ServerName serverName = ConsistentHashBalancer.calcServerNameForItem(consistentHashRing,
					consistentHashInput);
			serverByPrefix.put(prefix, serverName);//now region->server mapping is known
		}
		
		//level out any imbalances from the hashing
		HBaseBalanceLeveler<ByteRange> leveler = new HBaseBalanceLeveler<>(drhServerList.getServerNames(),
				serverByPrefix, tableName);
		serverByPrefix = leveler.getBalancedDestinationByItem();

		//map individual regions to servers based on their prefix
		for(Map.Entry<ByteRange,ServerName> entry : serverByPrefix.entrySet()){
			List<DrRegionInfo<?>> regionsInPrefix = regionsByPrefix.get(entry.getKey());
			for(DrRegionInfo<?> region : regionsInPrefix){
				serverByRegion.put(region, entry.getValue());
			}
		}
//		logger.warn(getServerByRegionStringForDebug());
		assertRegionCountsConsistent();
		return serverByRegion;
	}
	
	
	private void initRegionByPrefixMap(){
		regionsByPrefix = new TreeMap<>();
		for(List<Field<?>> prefixFields : scatteringPrefix.getAllPossibleScatteringPrefixes()){
			ByteRange prefix = new ByteRange(FieldTool.getConcatenatedValueBytes(prefixFields, false, false));
			regionsByPrefix.put(prefix, new ArrayList<DrRegionInfo<?>>()); 
		}
	}
	
	
	private void groupRegionsByPrefix(){
		//group the regions by prefix
		for(DrRegionInfo<?> drhRegionInfo : drhRegionList.getRegionsSorted()){
			byte[] startKey = drhRegionInfo.getRegion().getStartKey();
			if(DrArrayTool.isEmpty(startKey)){//use first prefix (is there a more robust way?)
				List<Field<?>> firstPrefix = scatteringPrefix.getAllPossibleScatteringPrefixes().get(0);
				startKey = FieldTool.getConcatenatedValueBytes(firstPrefix, false, false);
			}
			ByteRange regionPrefixBytes = new ByteRange(DrByteTool.copyOfRange(startKey, 0, scatteringPrefix
					.getNumPrefixBytes()));
			regionsByPrefix.get(regionPrefixBytes).add(drhRegionInfo);
		}
	}
	
}
