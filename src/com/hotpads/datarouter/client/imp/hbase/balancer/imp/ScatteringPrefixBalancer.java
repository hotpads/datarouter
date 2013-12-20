package com.hotpads.datarouter.client.imp.hbase.balancer.imp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.util.Bytes;

import com.hotpads.datarouter.client.imp.hbase.balancer.BalancerStrategy;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHRegionInfo;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHRegionList;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHServerList;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.prefix.ScatteringPrefix;
import com.hotpads.util.core.ByteTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.bytes.ByteRange;
import com.hotpads.util.core.java.ReflectionTool;

/*
 * assign each partition in full to a server, and hard-balance the number of regions
 */
public class ScatteringPrefixBalancer
implements BalancerStrategy{
	
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
		//collect the different prefix byte arrays
//		List<List<Field<?>>> prefixes = scatteringPrefix.getAllPossibleScatteringPrefixes();
//		List<byte[]> prefixByteArrays = ListTool.createArrayList();
//		for(List<Field<?>> prefix : prefixes){
//			byte[] prefixBytes = FieldSetTool.getConcatenatedValueBytes(prefix, false, false);
//			prefixByteArrays.add(prefixBytes);
//		}
		

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
		
		//TODO balance them somehow!!!  THIS CLASS IS INCOMPLETE

		List<ServerName> serverNames = drhServerList.getServerNamesSorted();
		int regionIndex=0;
		for(DRHRegionInfo<?> drhRegionInfo : drhRegionList.getRegionsSorted()){
			int serverIndex = regionIndex % CollectionTool.size(serverNames);
			serverByRegion.put(drhRegionInfo, serverNames.get(serverIndex));
			++regionIndex;
		}
		
		return this;
	}
	
	
	@Override
	public ServerName getServerName(DRHRegionInfo<?> drhRegionInfo) {
		return serverByRegion.get(drhRegionInfo);
	}
	
//	public void setScatteringPrefixClass(Class<? extends ScatteringPrefix> scatteringPrefixClass){
//		this.scatteringPrefixClass = scatteringPrefixClass;
//	}
	
}
