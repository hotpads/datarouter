package com.hotpads.datarouter.client.imp.hbase.cluster;

import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HServerLoad.RegionLoad;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.imp.hbase.compaction.CompactionInfo;
import com.hotpads.datarouter.client.imp.hbase.compaction.DrhCompactionScheduler;
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

public class DrRegionInfo<PK extends PrimaryKey<PK>> implements Comparable<DrRegionInfo<?>>{
	private static final Logger logger = LoggerFactory.getLogger(DrRegionInfo.class);

	private Integer regionNum;
	private String tableName;
	private String name;
	private HRegionInfo regionInfo;
	private ServerName serverName;
	private Node<PK,?> node;
	private DatabeanFieldInfo<PK,?,?> fieldInfo;
	private Integer partition;
	private FieldSet<?> startKey, endKey;
	private RegionLoad load;
	private ServerName balancerDestinationServer;
	private DrhCompactionScheduler<PK> compactionScheduler;

	public DrRegionInfo(Integer regionNum, String tableName, Class<PK> primaryKeyClass, HRegionInfo regionInfo,
			ServerName serverName, Node<PK,?> node, RegionLoad load, CompactionInfo compactionInfo){
		this.regionNum = regionNum;
		this.tableName = tableName;
		this.name = new String(regionInfo.getRegionName());
		this.regionInfo = regionInfo;
		this.serverName = serverName;
		this.node = node;
		this.fieldInfo = node.getFieldInfo();//set before calling getKey
		this.startKey = getKey(primaryKeyClass, regionInfo.getStartKey());
		this.endKey = getKey(primaryKeyClass, regionInfo.getEndKey());
		this.partition = calculatePartition(regionInfo.getStartKey());
		this.load = load;
		this.compactionScheduler = new DrhCompactionScheduler<>(compactionInfo, this);
	}


	/******************************* methods *****************************************/

	private FieldSet<?> getKey(Class<PK> primaryKeyClass, byte[] bytes){
		PK sampleKey = ReflectionTool.create(primaryKeyClass);
		if(DrArrayTool.isEmpty(bytes)){
			return sampleKey;
		}
		if(fieldInfo.isEntity()){
			HBaseSubEntityReaderNode<?,?,?,?,?> subEntityNode = (HBaseSubEntityReaderNode<?,?,?,?,?>)node;
			EntityKey<?> ek = subEntityNode.getResultParser().getEkFromRowBytes(bytes);
			return ek;
		}
		return HBaseResultTool.getPrimaryKeyUnchecked(bytes, fieldInfo);
	}

	private Integer calculatePartition(byte[] bytes){
		if(fieldInfo.isEntity()){
			if(DrArrayTool.isEmpty(bytes)){
				return 0;
			}
			HBaseSubEntityReaderNode<?,?,?,?,?> subEntityNode = (HBaseSubEntityReaderNode<?,?,?,?,?>)node;
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

	//used in hbaseTableRegions.jsp
	public String getDisplayServerName(){
		//doesn't account for multiple servers per node
		return getDisplayServerName(serverName.getHostname());//hServerInfo.getHostname();
	}

	private static String getDisplayServerName(String name){
		name = name.trim();
		name = name.replace("HadoopNode", "");
		name = name.replace(".hotpads.srv", "");
		return name;
	}

	//used in hbaseTableRegions.jsp
	public String getConsistentHashDisplayServerName(){
		//doesn't account for multiple servers per node
		return getDisplayServerName(balancerDestinationServer.getHostname());//hServerInfo.getHostname();
	}

	//used in hbaseTableRegions.jsp
	public String getNumKeyValuesWithCompactionPercent(){
		if(load == null){
			return "?";
		}
		long totalKvs = load.getTotalCompactingKVs();
		String totalKvsString = DrNumberFormatter.addCommas(totalKvs);
		long compactingKvs = load.getCurrentCompactedKVs();
		if(totalKvs == compactingKvs){
			return totalKvsString;
		}
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
		if(this == obj){
			return true;
		}
		if(ClassTool.differentClass(this, obj)){
			return false;
		}
		DrRegionInfo<?> that = (DrRegionInfo<?>)obj;
		return DrObjectTool.equals(regionInfo.getEncodedName(), that.regionInfo.getEncodedName());
	}

	@Override
	public int hashCode(){
		return regionInfo.getEncodedName().hashCode();
	}

	@Override
	public int compareTo(DrRegionInfo<?> other) {
		return Bytes.compareTo(regionInfo.getStartKey(), other.getRegion().getStartKey());
	}


	/********************************** get/set ******************************************/

	//used in hbaseTableRegions.jsp
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

	//used in hbaseTableRegions.jsp
	public FieldSet<?> getEndKey(){
		return endKey;
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

	//used in hbaseTableRegions.jsp
	public DrhCompactionScheduler<PK> getCompactionScheduler(){
		return compactionScheduler;
	}

	public void setBalancerDestinationServer(ServerName balancerDestinationServer){
		this.balancerDestinationServer = Preconditions.checkNotNull(balancerDestinationServer);
	}


	/********************************* tests ******************************************/

	public static class DrhRegionInfoTests{
		@Test
		public void testGetDisplayServerName(){
			String name = "HadoopNode101.hotpads.srv";
			name = getDisplayServerName(name);
			Assert.assertEquals("101", name);
		}
	}

}
