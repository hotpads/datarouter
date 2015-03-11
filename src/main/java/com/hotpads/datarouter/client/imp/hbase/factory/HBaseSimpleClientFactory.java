package com.hotpads.datarouter.client.imp.hbase.factory;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.io.encoding.DataBlockEncoding;
import org.apache.hadoop.hbase.io.hfile.Compression.Algorithm;
import org.apache.hadoop.hbase.regionserver.StoreFile.BloomType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.imp.hbase.HBaseClientImp;
import com.hotpads.datarouter.client.imp.hbase.node.HBaseReaderNode;
import com.hotpads.datarouter.client.imp.hbase.node.HBaseSubEntityReaderNode;
import com.hotpads.datarouter.client.imp.hbase.pool.HTableExecutorServicePool;
import com.hotpads.datarouter.client.imp.hbase.pool.HTablePerTablePool;
import com.hotpads.datarouter.client.imp.hbase.pool.HTablePool;
import com.hotpads.datarouter.client.imp.hbase.pool.HTableSharedPool;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseQueryBuilder;
import com.hotpads.datarouter.client.type.HBaseClient;
import com.hotpads.datarouter.exception.UnavailableException;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fieldcache.EntityFieldInfo;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.entity.EntityPartitioner;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.prefix.ScatteringPrefix;
import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrPropertiesTool;
import com.hotpads.util.core.bytes.ByteRange;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.collections.Twin;
import com.hotpads.util.core.profile.PhaseTimer;

public class HBaseSimpleClientFactory 
implements ClientFactory{
	Logger logger = LoggerFactory.getLogger(getClass());
	
	public static final Boolean PER_TABLE_POOL = false;//per table is less efficient
	public static final Boolean SHARED_POOL = false;//per table is less efficient

	//static caches
	public static Map<String,Configuration> CONFIG_BY_ZK_QUORUM = new ConcurrentHashMap<String,Configuration>();
	public static Map<Configuration,HBaseAdmin> ADMIN_BY_CONFIG = new ConcurrentHashMap<Configuration,HBaseAdmin>();
	
	static final Integer CREATE_CLIENT_TIMEOUT_MS = 20*1000;//Integer.MAX_VALUE;
	
	
	/********************* fields *******************************/
	
	protected DatarouterContext drContext;
//	protected List<PhysicalNode<?,?>> physicalNodes = ListTool.createArrayList();
	protected String clientName;
	protected Set<String> configFilePaths = new TreeSet<>();
	protected List<Properties> multiProperties = DrListTool.createArrayList();
	protected HBaseOptions options;
	protected volatile HBaseClient client;//volatile for double checked locking
	protected Configuration hBaseConfig;
	protected HBaseAdmin hBaseAdmin;
	
	protected List<String> historicClientIds = DrListTool.createArrayList();

	
	public HBaseSimpleClientFactory(
			DatarouterContext drContext,
			String clientName){
		this.drContext = drContext;
		this.clientName = clientName;

		this.configFilePaths = drContext.getConfigFilePaths();
		this.multiProperties = DrPropertiesTool.fromFiles(configFilePaths);
		this.options = new HBaseOptions(multiProperties, clientName);
	}
	
	
	@Override 
	public HBaseClient call(){
		if(client!=null){ return client; }
		HBaseClientImp newClient = null;
		try{
			logger.info("activating HBase client "+clientName);
			PhaseTimer timer = new PhaseTimer(clientName);

			String zkQuorum = options.zookeeperQuorum();
			hBaseConfig = CONFIG_BY_ZK_QUORUM.get(zkQuorum);
			if(hBaseConfig==null){
				hBaseConfig = HBaseConfiguration.create();
				hBaseConfig.set(HConstants.ZOOKEEPER_QUORUM, zkQuorum);
			}
			hBaseAdmin = new HBaseAdmin(hBaseConfig);
			if(hBaseAdmin.getConnection().isClosed()){
				CONFIG_BY_ZK_QUORUM.remove(zkQuorum);
				ADMIN_BY_CONFIG.remove(hBaseConfig);
				hBaseConfig = null;
				hBaseAdmin = null;
				String log = "couldn't open connection because hBaseAdmin.getConnection().isClosed()";
				logger.warn(log);
				throw new UnavailableException(log);
			}
			CONFIG_BY_ZK_QUORUM.put(zkQuorum, hBaseConfig);
			ADMIN_BY_CONFIG.put(hBaseConfig, hBaseAdmin);
	
			//databean config
			Pair<HTablePool,Map<String,Class<PrimaryKey<?>>>> result = initTables();
			timer.add("init HTables");
			
			newClient = new HBaseClientImp(clientName, options, hBaseConfig, hBaseAdmin, 
					result.getLeft(), result.getRight());
			logger.warn(timer.add("done").toString());
//					historicClientIds.add(System.identityHashCode(newClient)+"");
//					logger.warn("historicClientIds"+historicClientIds);
		}catch(ZooKeeperConnectionException e){
			throw new UnavailableException(e);
		}catch(MasterNotRunningException e){
			throw new UnavailableException(e);
		}
		return newClient;
	}
	
	
	public static final int 
		PER_TABLE_minPoolSize = 1,//these are per-table
		PER_TABLE_maxPoolSize = 5,
		EXECUTOR_SERVICE_maxPoolSize = 50;
	
	public static final long 
			DEFAULT_MAX_FILE_SIZE_BYTES = 1024 * 1024 * 1024,
			DEFAULT_MEMSTORE_FLUSH_SIZE_BYTES = 256 * 1024 * 1024;
	
	public static final byte[] DEFAULT_FAMILY_QUALIFIER = new byte[]{(byte)'a'};
	public static final String DUMMY_COL_NAME = new String(new byte[]{0});
	
	protected Pair<HTablePool,Map<String,Class<PrimaryKey<?>>>> initTables(){
		List<String> tableNames = DrListTool.create();
		Map<String,Class<PrimaryKey<?>>> primaryKeyClassByName = new HashMap<>();
		Map<String,PhysicalNode<?,?>> nodeByTableName = new TreeMap<>();
		Collection<PhysicalNode<?,?>> physicalNodes = drContext.getNodes().getPhysicalNodesForClient(clientName);
		for(PhysicalNode<?,?> node : physicalNodes){
			tableNames.add(node.getTableName());
			primaryKeyClassByName.put(node.getTableName(), (Class<PrimaryKey<?>>)node.getPrimaryKeyType());
			nodeByTableName.put(node.getTableName(), node);
		}

		try{		    
			//manually delete tables here
//		    if(admin.tableExists("Trace")){
//		    	admin.disableTable("Trace");
//		    	admin.deleteTable("Trace");
//		    }
//		    if(admin.tableExists("TraceThread")){
//		    	admin.disableTable("TraceThread");
//		    	admin.deleteTable("TraceThread");
//		    }
//		    if(admin.tableExists("TraceSpan")){
//		    	admin.disableTable("TraceSpan");
//		    	admin.deleteTable("TraceSpan");
//		    }
		
			boolean checkTables = options.checkTables();
			boolean createTables = options.createTables();
			if(checkTables || createTables){
				for(String tableName : DrIterableTool.nullSafe(tableNames)){
					byte[] tableNameBytes = StringByteTool.getUtf8Bytes(tableName);
					if(createTables && !hBaseAdmin.tableExists(tableName)){
						logger.warn("table " + tableName + " not found, creating it");
						HTableDescriptor hTable = new HTableDescriptor(tableName);
						hTable.setMaxFileSize(DEFAULT_MAX_FILE_SIZE_BYTES);
						hTable.setMemStoreFlushSize(DEFAULT_MEMSTORE_FLUSH_SIZE_BYTES);
						HColumnDescriptor family = new HColumnDescriptor(DEFAULT_FAMILY_QUALIFIER);
						family.setMaxVersions(1);
						family.setBloomFilterType(BloomType.NONE);
						family.setDataBlockEncoding(DataBlockEncoding.FAST_DIFF);
						family.setCompressionType(Algorithm.GZ);
						hTable.addFamily(family);
						byte[][] splitPoints = getSplitPoints(nodeByTableName.get(tableName));
						if(DrArrayTool.isEmpty(splitPoints)
								|| DrArrayTool.isEmpty(splitPoints[0])){//a single empty byte array
							hBaseAdmin.createTable(hTable);
						}else{
							//careful, as throwing strange split points in here can crash master
							// and corrupt meta table
							hBaseAdmin.createTable(hTable, splitPoints);
						}
						logger.warn("created table " + tableName);
					}else if(checkTables){
						if(!hBaseAdmin.tableExists(tableNameBytes)){
							logger.warn("table " + tableName + " not found");
							break;
						}
					}
				}
			}
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		
		HTablePool pool = null;
		if (PER_TABLE_POOL) {
			pool = new HTablePerTablePool(hBaseConfig, tableNames,
					options.minPoolSize(PER_TABLE_minPoolSize),
					PER_TABLE_maxPoolSize);
		}else if(SHARED_POOL){
			pool = new HTableSharedPool(hBaseConfig, clientName,
					EXECUTOR_SERVICE_maxPoolSize, primaryKeyClassByName);
		}else {
			pool = new HTableExecutorServicePool(hBaseConfig, clientName,
					EXECUTOR_SERVICE_maxPoolSize, primaryKeyClassByName);
		}
		
		return Pair.create(pool,primaryKeyClassByName);
	}

	
	
	/*
	 * this currently gets ignored on an empty table since there are no regions to split
	 * 
	 * 
	 */
	protected byte[][] getSplitPoints(PhysicalNode<?,?> node){
		if(node instanceof HBaseSubEntityReaderNode){
			HBaseSubEntityReaderNode<?,?,?,?,?> subEntityNode = (HBaseSubEntityReaderNode<?,?,?,?,?>)node;
			EntityFieldInfo<?,?> entityFieldInfo = subEntityNode.getEntityFieldInfo(); 
			EntityPartitioner<?> partitioner = entityFieldInfo.getEntityPartitioner();
			//remember to skip the first partition
			int numSplitPoints = partitioner.getNumPartitions() - 1;
			byte[][] splitPoints = new byte[numSplitPoints][];
			for(int i=1; i < partitioner.getAllPrefixes().size(); ++i){
				splitPoints[i-1] = partitioner.getPrefix(i);
			}
			return splitPoints;
		}else if(node instanceof HBaseReaderNode){
			DatabeanFieldInfo<?,?,?> fieldInfo = node.getFieldInfo();
			ScatteringPrefix sampleScatteringPrefix = fieldInfo.getSampleScatteringPrefix();
			if(sampleScatteringPrefix==null){ return null; }
			List<List<Field<?>>> allPrefixes = sampleScatteringPrefix.getAllPossibleScatteringPrefixes();
			int counter = 0;
			List<byte[]> splitPoints = DrListTool.create();
			for(List<Field<?>> prefixFields : allPrefixes){
				++counter;
				Twin<ByteRange> range = HBaseQueryBuilder.getStartEndBytesForPrefix(prefixFields, false);
				if( ! isSingleEmptyByte(range.getLeft().toArray())){
					splitPoints.add(range.getLeft().toArray());
				}
			}
			return splitPoints.toArray(new byte[splitPoints.size()][]);
		}else{
			throw new IllegalArgumentException("Node should be one of the above two types");
		}
	}
	
	protected boolean isSingleEmptyByte(byte[] bytes){
		if(DrArrayTool.length(bytes)!=1){ return false; }
		return bytes[0] == Byte.MIN_VALUE;
	}
	
	
//	public static void main(String... args){
//		List<String> emptyList = new ArrayList<>();
//		for(String s : IterableTool.nullSafe(emptyList)){
//			System.out.println("asdf");
//		}
//	}
}

















