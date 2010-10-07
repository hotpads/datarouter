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
import org.apache.hadoop.hbase.ClusterStatus;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HServerAddress;
import org.apache.hadoop.hbase.HServerInfo;
import org.apache.hadoop.hbase.HServerLoad;
import org.apache.hadoop.hbase.HServerLoad.RegionLoad;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;

import com.hotpads.datarouter.client.type.HBaseClient;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.SetTool;

public class DRHRegionList{

	protected List<String> tableNames;
	protected List<DRHRegionInfo> regions;
	
	public DRHRegionList(HBaseClient client, List<String> tableNames, Configuration config){
		this.tableNames = ListTool.nullSafe(tableNames);
		this.regions = ListTool.create();
		try{
			for(String tableName : IterableTool.nullSafe(tableNames)){
				HTable hTable = new HTable(config, tableName);
				Class<PrimaryKey<?>> primaryKeyClass = client.getPrimaryKeyClass(tableName);
				Map<HRegionInfo, HServerAddress> regionsInfo = hTable.getRegionsInfo();
				HBaseAdmin admin = new HBaseAdmin(config);
				ClusterStatus clusterStatus = admin.getClusterStatus();
				Collection<HServerInfo> servers = clusterStatus.getServerInfo();
				Map<String,RegionLoad> regionLoadByName = MapTool.createTreeMap();
				for(HServerInfo server : IterableTool.nullSafe(servers)){
					HServerLoad serverLoad = server.getLoad();
					Collection<RegionLoad> regionsLoad = serverLoad.getRegionsLoad();
					for(RegionLoad regionLoad : regionsLoad){
						String name = new String(regionLoad.getName());
						regionLoadByName.put(name, regionLoad);
					}
				}
				int regionNum = 0;
				for(HRegionInfo info : MapTool.nullSafe(regionsInfo).keySet()){
					String name = new String(info.getRegionName());
					RegionLoad load = regionLoadByName.get(name);
					regions.add(new DRHRegionInfo(regionNum++, tableName, primaryKeyClass, 
							info, regionsInfo.get(info), load));
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
	
	public SortedMap<String,List<DRHRegionInfo>> getRegionsByServerName(){
		SortedMap<String,List<DRHRegionInfo>> out = MapTool.createTreeMap();
		for(DRHRegionInfo region : regions){
			String serverName = region.getServerName();
			if(out.get(serverName)==null){ out.put(serverName, new LinkedList<DRHRegionInfo>()); }
			out.get(serverName).add(region);
		}
		return out;
	}
	
	public LinkedHashMap<String,List<DRHRegionInfo>> getRegionsGroupedBy(String groupBy){
		LinkedHashMap<String,List<DRHRegionInfo>> regionsByGroup = new LinkedHashMap<String,List<DRHRegionInfo>>();
		if(null==groupBy){
			regionsByGroup.put("all", regions);
		}else if("serverName".equals(groupBy)){
			for(Map.Entry<String,List<DRHRegionInfo>> entry : getRegionsByServerName().entrySet()){
				regionsByGroup.put(entry.getKey(), entry.getValue());
			}
		}
		return regionsByGroup;
		
	}
}
