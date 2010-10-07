package com.hotpads.datarouter.client.imp.hbase;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.ClusterStatus;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HServerAddress;
import org.apache.hadoop.hbase.HServerInfo;
import org.apache.hadoop.hbase.HServerLoad;
import org.apache.hadoop.hbase.HServerLoad.RegionLoad;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;

import com.hotpads.datarouter.client.imp.hbase.util.HBaseResultTool;
import com.hotpads.datarouter.client.type.HBaseClient;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.java.ReflectionTool;

public class DRHRegionInfo{

	protected Integer regionNum;
	protected String name;
	protected PrimaryKey<?> startKey, endKey;
	protected HRegionInfo region;
	protected HServerAddress server;
	protected RegionLoad load;
	
	
	public static List<DRHRegionInfo> createRegions(HBaseClient client, String tableName, Configuration config){
		try{
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
			List<DRHRegionInfo> outs = ListTool.create();
			int regionNum = 0;
			for(HRegionInfo info : MapTool.nullSafe(regionsInfo).keySet()){
				String name = new String(info.getRegionName());
				RegionLoad load = regionLoadByName.get(name);
				outs.add(new DRHRegionInfo(regionNum++, primaryKeyClass, info, regionsInfo.get(info), load));
			}
			return outs;
		}catch(IOException e){
			throw new DataAccessException(e);
		}
	}
	
	
	public DRHRegionInfo(Integer regionNum, Class<PrimaryKey<?>> primaryKeyClass, 
			HRegionInfo info, HServerAddress server, RegionLoad load){
		this.regionNum = regionNum;
		this.name = new String(info.getRegionName());
		this.region = info;
		this.server = server;
		this.startKey = getKey(primaryKeyClass, info.getStartKey());
		this.endKey = getKey(primaryKeyClass, info.getEndKey());
		this.load = load;
	}
	
	public static PrimaryKey<?> getKey(Class<PrimaryKey<?>> primaryKeyClass, byte[] bytes){
		PrimaryKey<?> sampleKey = ReflectionTool.create(primaryKeyClass);
		if(ArrayTool.isEmpty(bytes)){ return sampleKey; }
		return HBaseResultTool.getPrimaryKeyUnchecked(bytes, primaryKeyClass, sampleKey.getFields());
	}


	public Integer getRegionNum(){
		return regionNum;
	}


	public PrimaryKey<?> getStartKey(){
		return startKey;
	}


	public PrimaryKey<?> getEndKey(){
		return endKey;
	}


	public HServerAddress getServer(){
		return server;
	}


	public HRegionInfo getRegion(){
		return region;
	}


	public RegionLoad getLoad(){
		return load;
	}
	
	
	
}
