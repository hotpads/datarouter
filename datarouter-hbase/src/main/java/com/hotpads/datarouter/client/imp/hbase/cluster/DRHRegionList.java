package com.hotpads.datarouter.client.imp.hbase.cluster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HServerLoad;
import org.apache.hadoop.hbase.HServerLoad.RegionLoad;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.client.HTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.hbase.balancer.BaseHBaseRegionBalancer;
import com.hotpads.datarouter.client.imp.hbase.client.HBaseClient;
import com.hotpads.datarouter.client.imp.hbase.compaction.DRHCompactionInfo;
import com.hotpads.datarouter.client.imp.hbase.node.HBaseSubEntityReaderNode;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.storage.key.entity.EntityPartitioner;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.prefix.ScatteringPrefix;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrMapTool;
import com.hotpads.util.core.concurrent.CallableTool;

public class DRHRegionList{
	private static final Logger logger = LoggerFactory.getLogger(DRHRegionList.class);

	public static final Integer BUCKETS_PER_NODE = 1000;

	private DRHServerList servers;
	private String tableName;
	private Node<?,?> node;
	private List<DRHRegionInfo<?>> regions;
	private Class<? extends ScatteringPrefix> scatteringPrefixClass;
	private EntityPartitioner<?> entityPartitioner;
	private BaseHBaseRegionBalancer balancerStrategy;
	private Map<DRHRegionInfo<?>,ServerName> targetServerNameByRegion;
	private DRHCompactionInfo compactionInfo;

	@SuppressWarnings("unchecked")
	public DRHRegionList(HBaseClient client, DRHServerList servers, String tableName, Configuration config,
			Node<?,?> node, BaseHBaseRegionBalancer balancerStrategy, DRHCompactionInfo compactionInfo){
		this.servers = servers;
		this.tableName = tableName;
		this.node = node;
		this.compactionInfo = compactionInfo;
		this.regions = new ArrayList<>();
		this.scatteringPrefixClass = node.getFieldInfo().getScatteringPrefixClass();
		if(node.getFieldInfo().isEntity()){
			HBaseSubEntityReaderNode subEntityNode = (HBaseSubEntityReaderNode)node;
			this.entityPartitioner = subEntityNode.getEntityFieldInfo().getEntityPartitioner();
		}
		
		//TODO do less in constructor
		Class<PrimaryKey<?>> primaryKeyClass = client.getPrimaryKeyClass(tableName);
		Map<HRegionInfo,ServerName> serverNameByHRegionInfo = getServerNameByHRegionInfo(client, config, tableName);
		//this got reorganized in hbase 0.92... just making quick fix for now
		Map<String,RegionLoad> regionLoadByName = new TreeMap<>();
		for(DRHServerInfo server : DrIterableTool.nullSafe(servers.getServers())){
			HServerLoad serverLoad = server.gethServerLoad();
			Map<byte[],HServerLoad.RegionLoad> regionsLoad = serverLoad.getRegionsLoad();
			for(RegionLoad regionLoad : regionsLoad.values()){
//					String name = new String(regionLoad.getName());
				String name = HRegionInfo.encodeRegionName(regionLoad.getName());
				regionLoadByName.put(name, regionLoad);
			}
		}
		int regionNum = 0;
		for(HRegionInfo hRegionInfo : DrMapTool.nullSafe(serverNameByHRegionInfo).keySet()){
			try{
				RegionLoad regionLoad = regionLoadByName.get(hRegionInfo.getEncodedName());
				ServerName serverName = serverNameByHRegionInfo.get(hRegionInfo);
				HServerLoad hServerLoad = servers.getHServerLoad(serverName);
				regions.add(new DRHRegionInfo(regionNum++, tableName, primaryKeyClass, 
						hRegionInfo, serverName, hServerLoad,
						node, regionLoad, compactionInfo));
			}catch(RuntimeException e){
				logger.warn("couldn't build DRHRegionList for region:"+hRegionInfo.getEncodedName());
				throw e;
			}
		}
		Collections.sort(regions);//ensure sorted for getRegionsSorted
		this.balancerStrategy = balancerStrategy.init(scatteringPrefixClass, entityPartitioner, servers, this);
		this.targetServerNameByRegion = CallableTool.callUnchecked(balancerStrategy);
		balancerStrategy.assertRegionCountsConsistent();
		for(DRHRegionInfo<?> drhRegionInfo : regions){
			drhRegionInfo.setBalancerDestinationServer(targetServerNameByRegion.get(drhRegionInfo));
		}
	}
	
	private Map<HRegionInfo,ServerName> getServerNameByHRegionInfo(HBaseClient client, Configuration config, 
			String tableName){
		HTable hTable = null;
		try{
			hTable = new HTable(config, tableName);
//			logger.warn("got table "+tableName);
			Map<HRegionInfo,ServerName> serverNameByHRegionInfo = hTable.getRegionLocations();
//			logger.warn("got hTable.getRegionLocations()");
			return serverNameByHRegionInfo;
		}catch(IOException e){
			throw new DataAccessException(e);
		}finally{
			if(hTable != null){
				try{
					hTable.close();
				}catch(IOException e){
					throw new DataAccessException(e);
				}
			}
		}
	}
	

	public SortedSet<String> getServerNames(){
		SortedSet<String> serverNames = new TreeSet<>();
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
		SortedMap<String,List<DRHRegionInfo<?>>> out = new TreeMap<>();
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
