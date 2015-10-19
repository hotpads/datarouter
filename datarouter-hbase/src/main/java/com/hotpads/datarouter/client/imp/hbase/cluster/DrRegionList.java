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

public class DrRegionList{
	private static final Logger logger = LoggerFactory.getLogger(DrRegionList.class);

	public static final Integer BUCKETS_PER_NODE = 1000;

	private final String tableName;
	private final Node<?,?> node;
	private final List<DrRegionInfo<?>> regions;
	private final Class<? extends ScatteringPrefix> scatteringPrefixClass;
	private final EntityPartitioner<?> entityPartitioner;
	private final Map<DrRegionInfo<?>,ServerName> targetServerNameByRegion;

	public DrRegionList(HBaseClient client, DrServerList servers, String tableName, Configuration config,
			Node<?,?> node, BaseHBaseRegionBalancer balancerStrategy, DRHCompactionInfo compactionInfo){
		this.tableName = tableName;
		this.node = node;
		this.regions = new ArrayList<>();
		this.scatteringPrefixClass = node.getFieldInfo().getScatteringPrefixClass();
		if(node.getFieldInfo().isEntity()){
			HBaseSubEntityReaderNode<?,?,?,?,?> subEntityNode = (HBaseSubEntityReaderNode<?,?,?,?,?>)node;
			this.entityPartitioner = subEntityNode.getEntityFieldInfo().getEntityPartitioner();
		}else{
			this.entityPartitioner = null;
		}

		//TODO do less in constructor
		Class<? extends PrimaryKey<?>> primaryKeyClass = client.getPrimaryKeyClass(tableName);
		Map<HRegionInfo,ServerName> serverNameByHRegionInfo = getServerNameByHRegionInfo(config, tableName);
		//this got reorganized in hbase 0.92... just making quick fix for now
		Map<String,RegionLoad> regionLoadByName = new TreeMap<>();
		for(DrServerInfo server : DrIterableTool.nullSafe(servers.getServers())){
			HServerLoad serverLoad = server.gethServerLoad();
			Map<byte[],HServerLoad.RegionLoad> regionsLoad = serverLoad.getRegionsLoad();
			for(RegionLoad regionLoad : regionsLoad.values()){
				String name = HRegionInfo.encodeRegionName(regionLoad.getName());
				regionLoadByName.put(name, regionLoad);
			}
		}
		int regionNum = 0;
		for(HRegionInfo hregionInfo : DrMapTool.nullSafe(serverNameByHRegionInfo).keySet()){
			try{
				RegionLoad regionLoad = regionLoadByName.get(hregionInfo.getEncodedName());
				ServerName serverName = serverNameByHRegionInfo.get(hregionInfo);
				HServerLoad hserverLoad = servers.getHServerLoad(serverName);
				regions.add(new DrRegionInfo(regionNum++, tableName, primaryKeyClass,
						hregionInfo, serverName, hserverLoad,
						node, regionLoad, compactionInfo));
			}catch(RuntimeException e){
				logger.warn("couldn't build DRHRegionList for region:"+hregionInfo.getEncodedName());
				throw e;
			}
		}
		Collections.sort(regions);//ensure sorted for getRegionsSorted
		balancerStrategy.init(scatteringPrefixClass, entityPartitioner, servers, this);
		logger.info("starting balancerStrategy for {} with {} regions", tableName, regions.size());
		this.targetServerNameByRegion = CallableTool.callUnchecked(balancerStrategy);
		balancerStrategy.assertRegionCountsConsistent();
		for(DrRegionInfo<?> drhRegionInfo : regions){
			drhRegionInfo.setBalancerDestinationServer(targetServerNameByRegion.get(drhRegionInfo));
		}
	}

	private Map<HRegionInfo,ServerName> getServerNameByHRegionInfo(Configuration config, String tableName){
		HTable htable = null;
		try{
			htable = new HTable(config, tableName);
			Map<HRegionInfo,ServerName> serverNameByHRegionInfo = htable.getRegionLocations();
			return serverNameByHRegionInfo;
		}catch(IOException e){
			throw new DataAccessException(e);
		}finally{
			if(htable != null){
				try{
					htable.close();
				}catch(IOException e){
					throw new DataAccessException(e);
				}
			}
		}
	}

	public SortedSet<String> getServerNames(){
		SortedSet<String> serverNames = new TreeSet<>();
		for(DrRegionInfo<?> region : regions){
			serverNames.add(region.getServerName());
		}
		return serverNames;
	}

	public String getTableName(){
		return tableName;
	}

	public List<DrRegionInfo<?>> getRegions(){
		return regions;
	}

	public List<DrRegionInfo<?>> getRegionsSorted(){
		return regions;
	}

	public DrRegionInfo<?> getRegionByEncodedName(String encodedName){
		for(DrRegionInfo<?> region : regions){
			if(region.getRegion().getEncodedName().equals(encodedName)){
				return region;
			}
		}
		return null;
	}

	public DrRegionInfo<?> getRegionAfter(String encodedName){
		boolean foundFirstRegion = false;
		for(DrRegionInfo<?> region : regions){
			if(foundFirstRegion){
				return region;
			}
			if(region.getRegion().getEncodedName().equals(encodedName)){
				foundFirstRegion = true;
			}
		}
		return null;
	}

	public SortedMap<String,List<DrRegionInfo<?>>> getRegionsByServerName(){
		SortedMap<String,List<DrRegionInfo<?>>> out = new TreeMap<>();
		for(DrRegionInfo<?> region : regions){
			String serverName = region.getServerName();
			if(out.get(serverName) == null){
				out.put(serverName, new LinkedList<>());
			}
			out.get(serverName).add(region);
		}
		return out;
	}

	public LinkedHashMap<String,List<DrRegionInfo<?>>> getRegionsGroupedBy(String groupBy){
		LinkedHashMap<String,List<DrRegionInfo<?>>> regionsByGroup = new LinkedHashMap<>();
		if(null == groupBy){
			regionsByGroup.put("all", regions);
		}else if("serverName".equals(groupBy)){
			for(Map.Entry<String,List<DrRegionInfo<?>>> entry : getRegionsByServerName().entrySet()){
				regionsByGroup.put(entry.getKey(), entry.getValue());
			}
		}
		return regionsByGroup;
	}

	public ServerName getServerForRegion(DrRegionInfo<?> drhRegionInfo){
		return targetServerNameByRegion.get(drhRegionInfo);
	}

	public Node<?, ?> getNode() {
		return node;
	}

}
