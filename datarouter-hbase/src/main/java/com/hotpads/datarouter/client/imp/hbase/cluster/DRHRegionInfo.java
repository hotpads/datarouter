package com.hotpads.datarouter.client.imp.hbase.cluster;

import java.util.Random;

import org.junit.Assert;

import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HServerLoad;
import org.apache.hadoop.hbase.HServerLoad.RegionLoad;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.imp.hbase.compaction.DRHCompactionInfo;
import com.hotpads.datarouter.client.imp.hbase.compaction.DRHCompactionScheduler;
import com.hotpads.datarouter.client.imp.hbase.node.HBaseSubEntityReaderNode;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseResultTool;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.entity.EntityPartitioner;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.datarouter.util.core.ClassTool;
import com.hotpads.datarouter.util.core.DrNumberFormatter;
import com.hotpads.datarouter.util.core.DrObjectTool;
import com.hotpads.util.core.java.ReflectionTool;

public class DRHRegionInfo<PK extends PrimaryKey<PK>>
implements Comparable<DRHRegionInfo<?>>{
	private static final Logger logger = LoggerFactory.getLogger(DRHRegionInfo.class);
	
	private Integer regionNum;
	private String tableName;
	private String name;
	private HRegionInfo hRegionInfo;
	private ServerName serverName;
	private HServerLoad hServerLoad;
	private Node<?,?> node;
	private DatabeanFieldInfo<?,?,?> fieldInfo;
	private Integer partition;
	private FieldSet<?> startKey, endKey;
	private RegionLoad load;
	private byte[] consistentHashInput;
	private ServerName balancerDestinationServer;
	private DRHCompactionScheduler compactionScheduler;
	
	
	public DRHRegionInfo(Integer regionNum, String tableName, Class<PK> primaryKeyClass, 
			HRegionInfo hRegionInfo, ServerName serverName, HServerLoad hServerLoad, 
			Node<?,?> node, RegionLoad load, DRHCompactionInfo compactionInfo){
		this.regionNum = regionNum;
		this.tableName = tableName;
		this.name = new String(hRegionInfo.getRegionName());
		this.hRegionInfo = hRegionInfo;
		this.serverName = serverName;
		this.hServerLoad = hServerLoad;
		this.node = node;
		this.fieldInfo = node.getFieldInfo();//set before calling getKey
		this.startKey = getKey(primaryKeyClass, hRegionInfo.getStartKey());
		this.endKey = getKey(primaryKeyClass, hRegionInfo.getEndKey());
		this.partition = calculatePartition(hRegionInfo.getStartKey());
		this.load = load;
		this.consistentHashInput = hRegionInfo.getEncodedNameAsBytes();
		this.compactionScheduler = new DRHCompactionScheduler(compactionInfo, this);
	}
	
	
	/******************************* methods *****************************************/
	
	public FieldSet<?> getKey(Class<PK> primaryKeyClass, byte[] bytes){
		PK sampleKey = ReflectionTool.create(primaryKeyClass);
		if(DrArrayTool.isEmpty(bytes)){ return sampleKey; }
		if(fieldInfo.isEntity()){
			HBaseSubEntityReaderNode subEntityNode = (HBaseSubEntityReaderNode)node;
			EntityKey<?> ek = subEntityNode.getResultParser().getEkFromRowBytes(bytes);
			return ek;
		}
		return HBaseResultTool.getPrimaryKeyUnchecked(bytes, fieldInfo);
	}
	
	private Integer calculatePartition(byte[] bytes){
		if(fieldInfo.isEntity()){
			if(DrArrayTool.isEmpty(bytes)){ return 0; }
			HBaseSubEntityReaderNode subEntityNode = (HBaseSubEntityReaderNode)node;
			EntityPartitioner<?> partitioner = subEntityNode.getEntityFieldInfo().getEntityPartitioner();
			return partitioner.parsePartitionFromBytes(bytes);
		}
		return null;
	}
	
	public ServerName getConsistentHashServerName(){
		return balancerDestinationServer;
	}
	
	public boolean isOnCorrectServer(){
		try{
			return DrObjectTool.equals(serverName, balancerDestinationServer);
//					consistentHashHServer.getHostAndPort());
		}catch(NullPointerException npe){
			logger.warn("", npe);
		}
		return true;//default: leave it where it is
	}
	
	private static Random random = new Random();
	
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
		return getDisplayServerName(balancerDestinationServer.getHostname());//hServerInfo.getHostname();
	}
	
	public String getNumKeyValuesWithCompactionPercent(){
		if(load==null){ return "?"; }
		long totalKvs = load.getTotalCompactingKVs();
		String totalKvsString = DrNumberFormatter.addCommas(totalKvs);
		long compactingKvs = load.getCurrentCompactedKVs();
		if(totalKvs==compactingKvs){ return totalKvsString; }
		int percentCompacted = (int)((double)100 * (double)compactingKvs / totalKvs);
		return totalKvsString + " ["+percentCompacted+"%]";
	}
	
	
	/******************* Object, Comparable *******************************/
	
	@Override
	public String toString(){
		return hRegionInfo.getEncodedName();
	}
	
	public boolean equals(Object obj){
		if(this==obj){ return true; }
		if(ClassTool.differentClass(this, obj)){ return false; }
		DRHRegionInfo<PK> that = (DRHRegionInfo<PK>)obj;
		return DrObjectTool.equals(hRegionInfo.getEncodedName(), that.hRegionInfo.getEncodedName());
	}
	
	@Override
	public int hashCode(){
		return hRegionInfo.getEncodedName().hashCode();
	}
	
	@Override
	public int compareTo(DRHRegionInfo<?> o) {
		return Bytes.compareTo(hRegionInfo.getStartKey(), o.getRegion().getStartKey());
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

	public FieldSet<?> getStartKey(){
		return startKey;
	}

	public FieldSet<?> getEndKey(){
		return endKey;
	}
	
	public Integer getPartition(){
		return partition;
	}

	public HRegionInfo getRegion(){
		return hRegionInfo;
	}

	public RegionLoad getLoad(){
		return load;
	}
	
	public DRHCompactionScheduler getCompactionScheduler(){
		return compactionScheduler;
	}

//	public byte[] getConsistentHashInput(){
//		return consistentHashInput;
//	}
	
	public void setBalancerDestinationServer(ServerName balancerDestinationServer){
		this.balancerDestinationServer = Preconditions.checkNotNull(balancerDestinationServer);
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
