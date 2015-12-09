package com.hotpads.datarouter.client.imp.hbase.cluster;

import java.util.Random;

import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.RegionLoad;
import org.apache.hadoop.hbase.ServerLoad;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Assert;
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
import com.hotpads.datarouter.util.core.DrNumberFormatter;
import com.hotpads.datarouter.util.core.DrObjectTool;
import com.hotpads.util.core.java.ReflectionTool;
import com.hotpads.util.core.lang.ClassTool;

public class DrRegionInfo<PK extends PrimaryKey<PK>>
implements Comparable<DrRegionInfo<?>>{
	private static final Logger logger = LoggerFactory.getLogger(DrRegionInfo.class);

	private final Integer regionNum;
	private final String tableName;
	private final Class<PK> primaryKeyClass;
	private final String name;
	private final HRegionInfo regionInfo;
	private final ServerName serverName;
	private final ServerLoad serverLoad;
	private final Node<?,?> node;
	private final DatabeanFieldInfo<?,?,?> fieldInfo;
	private final Integer partition;
	private final RegionLoad load;
	private final byte[] consistentHashInput;
	private final DRHCompactionScheduler compactionScheduler;

	private ServerName balancerDestinationServer;


	public DrRegionInfo(Integer regionNum, String tableName, Class<PK> primaryKeyClass,
			HRegionInfo regionInfo, ServerName serverName, ServerLoad serverLoad,
			Node<?,?> node, RegionLoad load, DRHCompactionInfo compactionInfo){
		this.regionNum = regionNum;
		this.tableName = tableName;
		this.primaryKeyClass = primaryKeyClass;
		this.name = new String(regionInfo.getRegionName());
		this.regionInfo = regionInfo;
		this.serverName = serverName;
		this.serverLoad = serverLoad;
		this.node = node;
		this.fieldInfo = node.getFieldInfo();//set before calling getKey
		this.partition = calculatePartition(regionInfo.getStartKey());
		this.load = load;
		this.consistentHashInput = regionInfo.getEncodedNameAsBytes();
		this.compactionScheduler = new DRHCompactionScheduler(compactionInfo, this);
	}


	/******************************* methods *****************************************/

	public FieldSet<?> getKey(Class<PK> primaryKeyClass, byte[] bytes){
		PK sampleKey = ReflectionTool.create(primaryKeyClass);
		if(DrArrayTool.isEmpty(bytes)) {
			return sampleKey;
		}
		try{
			if(fieldInfo.isEntity()){
				HBaseSubEntityReaderNode subEntityNode = (HBaseSubEntityReaderNode)node;
				EntityKey<?> ek = subEntityNode.getResultParser().getEkFromRowBytes(bytes);
				return ek;
			}
			return HBaseResultTool.getPrimaryKeyUnchecked(bytes, fieldInfo);
		}catch(RuntimeException e){
			logger.warn("error on {}, {}", primaryKeyClass.getName(), Bytes.toStringBinary(bytes));
//			throw e;
			return null;
		}
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

	public ServerName getHBaseServerName(){
		return serverName;
	}

	public ServerName getBalancerDestinationHBaseServerName(){
		return balancerDestinationServer;
	}

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
		return regionInfo.getEncodedName();
	}

	@Override
	public boolean equals(Object obj){
		if(this==obj){ return true; }
		if(ClassTool.differentClass(this, obj)){ return false; }
		DrRegionInfo<PK> that = (DrRegionInfo<PK>)obj;
		return DrObjectTool.equals(regionInfo.getEncodedName(), that.regionInfo.getEncodedName());
	}

	@Override
	public int hashCode(){
		return regionInfo.getEncodedName().hashCode();
	}

	@Override
	public int compareTo(DrRegionInfo<?> o) {
		return Bytes.compareTo(regionInfo.getStartKey(), o.getRegion().getStartKey());
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
		return getKey(primaryKeyClass, regionInfo.getStartKey());
	}

	public FieldSet<?> getEndKey(){
		return getKey(primaryKeyClass, regionInfo.getEndKey());
	}

	public Integer getPartition(){
		return partition;
	}

	public HRegionInfo getRegion(){
		return regionInfo;
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

	public static class DrRegionInfoTests{
		@Test public void testGetDisplayServerName(){
			String name = "HadoopNode101.hotpads.srv";
			name = getDisplayServerName(name);
			Assert.assertEquals("101", name);
		}
	}

}
