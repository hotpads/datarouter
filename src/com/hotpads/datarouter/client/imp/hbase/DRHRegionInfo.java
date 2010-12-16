package com.hotpads.datarouter.client.imp.hbase;

import java.util.Random;

import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HServerAddress;
import org.apache.hadoop.hbase.HServerLoad.RegionLoad;

import com.hotpads.datarouter.client.imp.hbase.util.HBaseResultTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.java.ReflectionTool;

public class DRHRegionInfo{

	protected Integer regionNum;
	protected String tableName;
	protected String name;
	protected PrimaryKey<?> startKey, endKey;
	protected HRegionInfo region;
	protected HServerAddress server;
	protected RegionLoad load;
	
	
	public DRHRegionInfo(Integer regionNum, String tableName, Class<PrimaryKey<?>> primaryKeyClass, 
			HRegionInfo info, HServerAddress server, RegionLoad load){
		this.regionNum = regionNum;
		this.tableName = tableName;
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


	public String getTableName(){
		return tableName;
	}

	public String getName(){
		return name;
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
	
	protected static Random random = new Random();
	
	public String getServerName(){
		String name = server.getHostname();
//		if("manimal".equals(name)){ name += random.nextInt(3); }
		return name;
	}
	
	public String getDisplayServerName(){
		String name = getServerName();
		name = name.replace("HadoopNode", "");
		return name;
	}


	public HRegionInfo getRegion(){
		return region;
	}


	public RegionLoad getLoad(){
		return load;
	}
	
	
	
}
