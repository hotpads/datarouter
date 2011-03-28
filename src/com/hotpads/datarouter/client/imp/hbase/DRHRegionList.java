package com.hotpads.datarouter.client.imp.hbase;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HServerAddress;
import org.apache.hadoop.hbase.HServerInfo;
import org.apache.hadoop.hbase.HServerLoad;
import org.apache.hadoop.hbase.HServerLoad.RegionLoad;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.imp.hbase.util.CompactionInfo;
import com.hotpads.datarouter.client.type.HBaseClient;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.HashMethods;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.SetTool;

public class DRHRegionList{
	Logger logger = Logger.getLogger(DRHRegionList.class);

	public static final Integer BUCKETS_PER_NODE = 1000;

	protected List<String> tableNames;
	protected List<DRHRegionInfo> regions;
	protected SortedMap<Long,DRHServerInfo> consistentHashRing;
	protected CompactionInfo compactionInfo;

	public DRHRegionList(HBaseClient client, List<String> tableNames, Configuration config,
			CompactionInfo compactionInfo){
		this.tableNames = ListTool.nullSafe(tableNames);
		this.compactionInfo = compactionInfo;
		this.regions = ListTool.create();
		try{
			for(String tableName : IterableTool.nullSafe(tableNames)){
				HTable hTable = new HTable(config, tableName);
				Class<PrimaryKey<?>> primaryKeyClass = client.getPrimaryKeyClass(tableName);
				Map<HRegionInfo,HServerAddress> hServerAddressByHRegionInfo = hTable.getRegionsInfo();

				DRHServerList servers = new DRHServerList(config);
				this.consistentHashRing = MapTool.createTreeMap();
				for(DRHServerInfo server : servers.getServers()){
					for(int i = 0; i < BUCKETS_PER_NODE; ++i){
						long bucketPosition = HashMethods.longMD5DJBHash(
								server.getHserverInfo().getHostnamePort()+i);
						consistentHashRing.put(bucketPosition, server);
					}
				}

				Map<String,RegionLoad> regionLoadByName = MapTool.createTreeMap();
				for(DRHServerInfo server : IterableTool.nullSafe(servers.getServers())){
					HServerLoad serverLoad = server.getHserverInfo().getLoad();
					Collection<RegionLoad> regionsLoad = serverLoad.getRegionsLoad();
					for(RegionLoad regionLoad : regionsLoad){
						String name = new String(regionLoad.getName());
						regionLoadByName.put(name, regionLoad);
					}
				}
				int regionNum = 0;
				for(HRegionInfo info : MapTool.nullSafe(hServerAddressByHRegionInfo).keySet()){
					String name = new String(info.getRegionName());
					RegionLoad load = regionLoadByName.get(name);
					HServerAddress hServerAddress = hServerAddressByHRegionInfo.get(info);
					HServerInfo hServerInfo = servers.getHServerInfo(hServerAddress);
					regions.add(new DRHRegionInfo(regionNum++, tableName, primaryKeyClass, info, hServerInfo,
							hServerAddress, this, load, compactionInfo));
				}
			}
		}catch(IOException e){
			throw new DataAccessException(e);
		}
	}

	public boolean getHasMultipleTables(){
		return CollectionTool.size(tableNames) > 1;
	}

	public SortedSet<String> getServerNames(){
		SortedSet<String> serverNames = SetTool.createTreeSet();
		for(DRHRegionInfo region : regions){
			serverNames.add(region.getServerName());
		}
		return serverNames;
	}

	public List<String> getTableNames(){
		return tableNames;
	}

	public List<DRHRegionInfo> getRegions(){
		return regions;
	}

	public DRHRegionInfo getRegionByEncodedName(String encodedName){
		for(DRHRegionInfo region : regions){
			if(region.getRegion().getEncodedName().equals(encodedName)){ return region; }
		}
		return null;
	}

	public DRHRegionInfo getRegionAfter(String encodedName){
		boolean foundFirstRegion = false;
		for(DRHRegionInfo region : regions){
			if(foundFirstRegion){ return region; }
			if(region.getRegion().getEncodedName().equals(encodedName)){ foundFirstRegion = true; }
		}
		return null;
	}

	public SortedMap<String,List<DRHRegionInfo>> getRegionsByServerName(){
		SortedMap<String,List<DRHRegionInfo>> out = MapTool.createTreeMap();
		for(DRHRegionInfo region : regions){
			String serverName = region.getServerName();
			if(out.get(serverName) == null){
				out.put(serverName, new LinkedList<DRHRegionInfo>());
			}
			out.get(serverName).add(region);
		}
		return out;
	}

	public LinkedHashMap<String,List<DRHRegionInfo>> getRegionsGroupedBy(String groupBy){
		LinkedHashMap<String,List<DRHRegionInfo>> regionsByGroup = new LinkedHashMap<String,List<DRHRegionInfo>>();
		if(null == groupBy){
			regionsByGroup.put("all", regions);
		}else if("serverName".equals(groupBy)){
			for(Map.Entry<String,List<DRHRegionInfo>> entry : getRegionsByServerName().entrySet()){
				regionsByGroup.put(entry.getKey(), entry.getValue());
			}
		}
		return regionsByGroup;
	}

	public DRHServerInfo getServerForRegion(byte[] regionConsistentHashInput){
		long hash = HashMethods.longMD5DJBHash(regionConsistentHashInput);
		if(consistentHashRing.isEmpty()){ return null; }
		if(!consistentHashRing.containsKey(hash)){
			SortedMap<Long,DRHServerInfo> tail = consistentHashRing.tailMap(hash);
			hash = tail.isEmpty() ? consistentHashRing.firstKey() : tail.firstKey();
		}
		return consistentHashRing.get(hash);
	}
}
