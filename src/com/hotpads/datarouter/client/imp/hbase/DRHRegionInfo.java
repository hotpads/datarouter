package com.hotpads.datarouter.client.imp.hbase;

import java.util.Random;

import junit.framework.Assert;

import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HServerAddress;
import org.apache.hadoop.hbase.HServerLoad;
import org.apache.hadoop.hbase.HServerLoad.RegionLoad;
import org.apache.hadoop.hbase.ServerName;
import org.apache.log4j.Logger;
import org.junit.Test;

import com.hotpads.datarouter.client.imp.hbase.util.CompactionInfo;
import com.hotpads.datarouter.client.imp.hbase.util.CompactionScheduler;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseResultTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.NumberFormatter;
import com.hotpads.util.core.ObjectTool;
import com.hotpads.util.core.java.ReflectionTool;

public class DRHRegionInfo<PK extends PrimaryKey<PK>>{
	static Logger logger = Logger.getLogger(DRHRegionInfo.class);
	
	public static final Integer NUM_VNODES = 1 << 16;

	protected Integer regionNum;
	protected String tableName;
	protected String name;
	protected PK startKey, endKey;
	protected HRegionInfo hRegionInfo;
	protected ServerName serverName;
	protected HServerLoad hServerLoad;
	protected DRHRegionList regionList;
	protected DRHServerInfo consistentHashHServer;
	protected RegionLoad load;
	protected byte[] consistentHashInput;
	protected CompactionScheduler compactionScheduler;
	
	
	public DRHRegionInfo(Integer regionNum, String tableName, Class<PK> primaryKeyClass, 
			HRegionInfo hRegionInfo, ServerName serverName, HServerLoad hServerLoad, 
			DRHRegionList regionList, RegionLoad load, CompactionInfo compactionInfo){
		this.regionNum = regionNum;
		this.tableName = tableName;
		this.name = new String(hRegionInfo.getRegionName());
		this.hRegionInfo = hRegionInfo;
		this.serverName = serverName;
		this.hServerLoad = hServerLoad;
		this.regionList = regionList;
		this.startKey = getKey(primaryKeyClass, hRegionInfo.getStartKey());
		this.endKey = getKey(primaryKeyClass, hRegionInfo.getEndKey());
		this.load = load;
		this.consistentHashInput = hRegionInfo.getEncodedNameAsBytes();
//		this.consistentHashInput = ByteTool.concatenate(
//				StringByteTool.getUtf8Bytes(tableName), hRegionInfo.getStartKey());
		this.consistentHashHServer = regionList.getServerForRegion(consistentHashInput);
		this.compactionScheduler = new CompactionScheduler(compactionInfo, this);
	}
	
	
	/******************************* methods *****************************************/
	
	public PK getKey(Class<PK> primaryKeyClass, byte[] bytes){
		PK sampleKey = ReflectionTool.create(primaryKeyClass);
		if(ArrayTool.isEmpty(bytes)){ return sampleKey; }
		return HBaseResultTool.getPrimaryKeyUnchecked(bytes, regionList.getNode().getFieldInfo());
	}
	
	public DRHServerInfo getConsistentHashServer(){
		return consistentHashHServer;
	}
	
	public ServerName getConsistentHashServerAddress(){
		return consistentHashHServer.getServerName();
	}
	
	public boolean isOnCorrectServer(){
		try{
			return ObjectTool.equals(serverName.getHostAndPort(), 
					consistentHashHServer.getServerName().getHostAndPort());
		}catch(NullPointerException npe){//not sure where these are coming from yet
			logger.warn(ExceptionTool.getStackTraceAsString(npe));
		}
		return true;//default: leave it where it is
	}
	
	protected static Random random = new Random();
	
	public String getServerName(){
		String name = serverName.getServerName();
//		if("manimal".equals(name)){ name += random.nextInt(3); }
		return name;
	}
	
	public String getDisplayServerName(){
		//doesn't account for multiple servers per node
		return getDisplayServerName(serverName.getHostname());//hServerInfo.getHostname();
	}
	
	public String getConsistentHashDisplayServerName(){
		//doesn't account for multiple servers per node
		return getDisplayServerName(consistentHashHServer.getHostname());//hServerInfo.getHostname();
	}
	
	public String getNumKeyValuesWithCompactionPercent(){
		long totalKvs = load.getTotalCompactingKVs();
		String totalKvsString = NumberFormatter.addCommas(totalKvs);
		long compactingKvs = load.getCurrentCompactedKVs();
		if(totalKvs==compactingKvs){ return totalKvsString; }
		int percentCompacted = (int)((double)100 * (double)compactingKvs / (double)totalKvs);
		return totalKvsString + " ["+percentCompacted+"%]";
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

	public PK getStartKey(){
		return startKey;
	}

	public PK getEndKey(){
		return endKey;
	}

	public HRegionInfo getRegion(){
		return hRegionInfo;
	}

	public RegionLoad getLoad(){
		return load;
	}
	
	public CompactionScheduler getCompactionScheduler(){
		return compactionScheduler;
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
