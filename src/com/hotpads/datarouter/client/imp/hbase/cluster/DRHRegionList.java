package com.hotpads.datarouter.client.imp.hbase.cluster;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HServerLoad;
import org.apache.hadoop.hbase.HServerLoad.RegionLoad;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.imp.hbase.balancer.BaseHBaseRegionBalancer;
import com.hotpads.datarouter.client.imp.hbase.compaction.DRHCompactionInfo;
import com.hotpads.datarouter.client.type.HBaseClient;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.prefix.ScatteringPrefix;
import com.hotpads.util.core.CallableTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.SetTool;

public class DRHRegionList{
	Logger logger = Logger.getLogger(DRHRegionList.class);

	public static final Integer BUCKETS_PER_NODE = 1000;

	protected DRHServerList servers;
	protected String tableName;
	protected Node<?,?> node;
	protected List<DRHRegionInfo<?>> regions;
	protected Class<? extends ScatteringPrefix> scatteringPrefixClass;
	protected BaseHBaseRegionBalancer balancerStrategy;
	protected Map<DRHRegionInfo<?>,ServerName> targetServerNameByRegion;
	protected DRHCompactionInfo compactionInfo;

	@SuppressWarnings("unchecked")
	public DRHRegionList(HBaseClient client, DRHServerList servers, String tableName, Configuration config,
			Node<?,?> node, BaseHBaseRegionBalancer balancerStrategy, DRHCompactionInfo compactionInfo){
		this.servers = servers;
		this.tableName = tableName;
		this.node = node;
		this.compactionInfo = compactionInfo;
		this.regions = ListTool.create();
		try{
			HTable hTable = new HTable(config, tableName);
			Class<PrimaryKey<?>> primaryKeyClass = client.getPrimaryKeyClass(tableName);
			Map<HRegionInfo,ServerName> serverNameByHRegionInfo = hTable.getRegionLocations();

			//this got reorganized in hbase 0.92... just making quick fix for now
			Map<String,RegionLoad> regionLoadByName = MapTool.createTreeMap();
			for(DRHServerInfo server : IterableTool.nullSafe(servers.getServers())){
				HServerLoad serverLoad = server.gethServerLoad();
				Map<byte[],HServerLoad.RegionLoad> regionsLoad = serverLoad.getRegionsLoad();
				for(RegionLoad regionLoad : regionsLoad.values()){
					String name = new String(regionLoad.getName());
					regionLoadByName.put(name, regionLoad);
				}
			}
			int regionNum = 0;
			for(HRegionInfo hRegionInfo : MapTool.nullSafe(serverNameByHRegionInfo).keySet()){
				try{
					RegionLoad regionLoad = regionLoadByName.get(hRegionInfo.getEncodedName());
					ServerName serverName = serverNameByHRegionInfo.get(hRegionInfo);
					HServerLoad hServerLoad = servers.getHServerLoad(serverName);
					regions.add(new DRHRegionInfo(regionNum++, tableName, primaryKeyClass, 
							hRegionInfo, serverName, hServerLoad,
							this, regionLoad, compactionInfo));
				}catch(RuntimeException e){
					logger.warn("couldn't build DRHRegionList for region:"+hRegionInfo.getEncodedName());
					throw e;
				}
			}
		}catch(IOException e){
			throw new DataAccessException(e);
		}
		Collections.sort(regions);//ensure sorted for getRegionsSorted
		this.balancerStrategy = balancerStrategy.init(scatteringPrefixClass, servers, this);
		this.targetServerNameByRegion = CallableTool.callUnchecked(balancerStrategy);
		balancerStrategy.assertRegionCountsConsistent();
		for(DRHRegionInfo<?> drhRegionInfo : regions){
			drhRegionInfo.setBalancerDestinationServer(targetServerNameByRegion.get(drhRegionInfo));
		}
	}
	

	public SortedSet<String> getServerNames(){
		SortedSet<String> serverNames = SetTool.createTreeSet();
		for(DRHRegionInfo<?> region : regions){
			serverNames.add(region.getServerName());
		}
		return serverNames;
	}

	public String getTableName(){
		return tableName;
	}

	public List<DRHRegionInfo<?>> getRegions(){
		return regions;
	}

	public List<DRHRegionInfo<?>> getRegionsSorted(){
		return regions;
	}

	public DRHRegionInfo<?> getRegionByEncodedName(String encodedName){
		for(DRHRegionInfo<?> region : regions){
			if(region.getRegion().getEncodedName().equals(encodedName)){ return region; }
		}
		return null;
	}

	public DRHRegionInfo<?> getRegionAfter(String encodedName){
		boolean foundFirstRegion = false;
		for(DRHRegionInfo<?> region : regions){
			if(foundFirstRegion){ return region; }
			if(region.getRegion().getEncodedName().equals(encodedName)){ foundFirstRegion = true; }
		}
		return null;
	}

	public SortedMap<String,List<DRHRegionInfo<?>>> getRegionsByServerName(){
		SortedMap<String,List<DRHRegionInfo<?>>> out = MapTool.createTreeMap();
		for(DRHRegionInfo<?> region : regions){
			String serverName = region.getServerName();
			if(out.get(serverName) == null){
				out.put(serverName, new LinkedList<DRHRegionInfo<?>>());
			}
			out.get(serverName).add(region);
		}
		return out;
	}

	public LinkedHashMap<String,List<DRHRegionInfo<?>>> getRegionsGroupedBy(String groupBy){
		LinkedHashMap<String,List<DRHRegionInfo<?>>> regionsByGroup = new LinkedHashMap<String,List<DRHRegionInfo<?>>>();
		if(null == groupBy){
			regionsByGroup.put("all", regions);
		}else if("serverName".equals(groupBy)){
			for(Map.Entry<String,List<DRHRegionInfo<?>>> entry : getRegionsByServerName().entrySet()){
				regionsByGroup.put(entry.getKey(), entry.getValue());
			}
		}
		return regionsByGroup;
	}

//	public DRHServerInfo getServerForRegion(byte[] regionConsistentHashInput){
//		long hash = HashMethods.longMD5DJBHash(regionConsistentHashInput);
//		if(consistentHashRing.isEmpty()){ return null; }
//		if(!consistentHashRing.containsKey(hash)){
//			SortedMap<Long,DRHServerInfo> tail = consistentHashRing.tailMap(hash);
//			hash = tail.isEmpty() ? consistentHashRing.firstKey() : tail.firstKey();
//		}
//		return consistentHashRing.get(hash);
//	}
	
	public ServerName getServerForRegion(DRHRegionInfo drhRegionInfo){
		return targetServerNameByRegion.get(drhRegionInfo);
	}

	public Node<?, ?> getNode() {
		return node;
	}
	
	
}
