package com.hotpads.datarouter.client.imp.hbase;

import java.util.Random;

import junit.framework.Assert;

import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HServerAddress;
import org.apache.hadoop.hbase.HServerInfo;
import org.apache.hadoop.hbase.HServerLoad.RegionLoad;
import org.junit.Test;

import com.hotpads.datarouter.client.imp.hbase.util.HBaseResultTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.ByteTool;
import com.hotpads.util.core.ObjectTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.java.ReflectionTool;

public class DRHRegionInfo{
	
	public static final Integer NUM_VNODES = 1 << 16;

	protected Integer regionNum;
	protected String tableName;
	protected String name;
	protected PrimaryKey<?> startKey, endKey;
	protected HRegionInfo hRegionInfo;
	protected HServerInfo hServerInfo;
	@Deprecated HServerAddress hServerAddress;//should get from hServerInfo, but workaround for DNS issues - mcorgan 20110106
	protected DRHServerInfo consistentHashHServer;
	protected RegionLoad load;
	protected byte[] consistentHashInput;
	
	
	public DRHRegionInfo(Integer regionNum, String tableName, Class<PrimaryKey<?>> primaryKeyClass, 
			HRegionInfo hRegionInfo, HServerInfo hServerInfo, HServerAddress hServerAddress, 
			DRHRegionList regionList, RegionLoad load){
		this.regionNum = regionNum;
		this.tableName = tableName;
		this.name = new String(hRegionInfo.getRegionName());
		this.hRegionInfo = hRegionInfo;
		this.hServerInfo = hServerInfo;
		this.hServerAddress = hServerAddress;
		this.startKey = getKey(primaryKeyClass, hRegionInfo.getStartKey());
		this.endKey = getKey(primaryKeyClass, hRegionInfo.getEndKey());
		this.load = load;
		byte[][] hashInputBuilder = new byte[20][];
		for(int i=0; i < 20;){
			hashInputBuilder[i++] = StringByteTool.getUtf8Bytes(tableName);
			hashInputBuilder[i++] = hRegionInfo.getStartKey();
		}
		this.consistentHashInput = ByteTool.concatenate(hashInputBuilder);
		this.consistentHashHServer = regionList.getServerForRegion(consistentHashInput);
	}
	
	
	/******************************* methods *****************************************/
	
	public static PrimaryKey<?> getKey(Class<PrimaryKey<?>> primaryKeyClass, byte[] bytes){
		PrimaryKey<?> sampleKey = ReflectionTool.create(primaryKeyClass);
		if(ArrayTool.isEmpty(bytes)){ return sampleKey; }
		return HBaseResultTool.getPrimaryKeyUnchecked(bytes, primaryKeyClass, sampleKey.getFields());
	}

	public HServerAddress getServerAddress(){
		return hServerInfo.getServerAddress();
	}
	
	public DRHServerInfo getConsistentHashServer(){
		return consistentHashHServer;
	}
	
	public HServerAddress getConsistentHashServerAddress(){
		return consistentHashHServer.getHserverInfo().getServerAddress();
	}
	
	public boolean isOnCorrectServer(){
		return ObjectTool.equals(hServerInfo.getHostnamePort(), 
				consistentHashHServer.getHserverInfo().getHostnamePort());
	}
	
	protected static Random random = new Random();
	
	public String getServerName(){
		String name = hServerInfo.getServerName();
//		if("manimal".equals(name)){ name += random.nextInt(3); }
		return name;
	}
	
	public String getDisplayServerName(){
		//doesn't account for multiple servers per node
		return getDisplayServerName(hServerAddress.getHostname());//hServerInfo.getHostname();
	}
	
	public String getConsistentHashDisplayServerName(){
		//doesn't account for multiple servers per node
		return getDisplayServerName(consistentHashHServer.getHostname());//hServerInfo.getHostname();
	}

	
	/********************************** get/set ******************************************/

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

	public HRegionInfo getRegion(){
		return hRegionInfo;
	}

	public RegionLoad getLoad(){
		return load;
	}

	public byte[] getConsistentHashInput(){
		return consistentHashInput;
	}


	/********************************* static *************************************/
	
	public static String getDisplayServerName(String name){
		name = name.trim();
		name = name.replace("HadoopNode", "");
		name = name.replace(".hotpads.srv", "");
		return name;
	}
	
	
	/********************************* tests ******************************************/
	
	public static class DRHRegionInfoTests{
		@Test public void testGetDisplayServerName(){
			String name = "HadoopNode101.hotpads.srv";
			name = getDisplayServerName(name);
			Assert.assertEquals("101", name);
		}
	}
	
}
