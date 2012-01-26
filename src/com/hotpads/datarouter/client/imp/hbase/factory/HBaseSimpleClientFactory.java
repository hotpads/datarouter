package com.hotpads.datarouter.client.imp.hbase.factory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.io.hfile.Compression.Algorithm;
import org.apache.hadoop.hbase.regionserver.StoreFile.BloomType;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.hbase.HBaseClientImp;
import com.hotpads.datarouter.client.imp.hbase.HTableExecutorServicePool;
import com.hotpads.datarouter.client.imp.hbase.HTablePerTablePool;
import com.hotpads.datarouter.client.imp.hbase.HTablePool;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseQueryBuilder;
import com.hotpads.datarouter.client.type.HBaseClient;
import com.hotpads.datarouter.exception.UnavailableException;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.field.SimpleFieldSet;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.prefix.ScatteringPrefix;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.PropertiesTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.profile.PhaseTimer;

public class HBaseSimpleClientFactory 
implements HBaseClientFactory{
	Logger logger = Logger.getLogger(getClass());
	
	public static final Boolean PER_TABLE_POOL = false;//per table is less efficient
	
	//static caches
	public static Map<String,Configuration> CONFIG_BY_ZK_QUORUM = new ConcurrentHashMap<String,Configuration>();
	public static Map<Configuration,HBaseAdmin> ADMIN_BY_CONFIG = new ConcurrentHashMap<Configuration,HBaseAdmin>();
	
	static final Integer CREATE_CLIENT_TIMEOUT_MS = 20*1000;//Integer.MAX_VALUE;
	
	protected DataRouter router;
	protected String clientName;
	protected String configFileLocation;
	protected ExecutorService executorService;
	protected Properties properties;
	protected HBaseOptions options;
	protected HBaseClient client;
	protected Configuration hBaseConfig;
	protected HBaseAdmin hBaseAdmin;
	
	protected List<String> historicClientIds = ListTool.createArrayList();

	
	public HBaseSimpleClientFactory(
			DataRouter router, String clientName, 
			String configFileLocation, 
			ExecutorService executorService){
		this.router = router;
		this.clientName = clientName;
		this.configFileLocation = configFileLocation;
		this.executorService = executorService;
		this.properties = PropertiesTool.ioAndNullSafeFromFile(configFileLocation);
		this.options = new HBaseOptions(properties, clientName);
	}
	
	@Override
	public HBaseClient getClient(){
		if(client!=null){ return client; }
		synchronized(this){
			if(client!=null){ return client; }
			Future<HBaseClient> future = executorService.submit(new HBaseClientBuilder());
			try{
				this.client = future.get(CREATE_CLIENT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
			}catch(InterruptedException e){
				throw new RuntimeException(e);
			}catch(ExecutionException e){
				throw new RuntimeException(e);
			} catch(TimeoutException e) {
				logger.warn("couldn't instantiate client "+clientName+" in "+CREATE_CLIENT_TIMEOUT_MS+"ms");
				future.cancel(false);
				if(!ClientType.USE_RECONNECTING_HBASE_CLIENT){
					throw new RuntimeException(e);
				}
			}
		}
		return client;
	}
	
	
	public class HBaseClientBuilder implements Callable<HBaseClient>{
		@Override public HBaseClient call(){
			if(client!=null){ return client; }
			HBaseClientImp newClient = null;
			try{
				logger.warn("activating HBase client "+clientName);
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
				}else{//yay, the connection is open
					CONFIG_BY_ZK_QUORUM.put(zkQuorum, hBaseConfig);
					ADMIN_BY_CONFIG.put(hBaseConfig, hBaseAdmin);
				}
		
				//databean config
				Pair<HTablePool,Map<String,Class<PrimaryKey<?>>>> result = initTables();
				timer.add("init HTables");
				
				newClient = new HBaseClientImp(clientName, options, hBaseConfig, hBaseAdmin, 
						result.getLeft(), result.getRight());
				logger.warn(timer.add("done"));
//					historicClientIds.add(System.identityHashCode(newClient)+"");
//					logger.warn("historicClientIds"+historicClientIds);
			}catch(ZooKeeperConnectionException e){
				throw new UnavailableException(e);
			}catch(MasterNotRunningException e){
				throw new UnavailableException(e);
			}
			return newClient;
		}
	}
	
	
	public static final int 
		DEFAULT_minPoolSize = 1,//these are per-table
		DEFAULT_maxPoolSize = 5,
		EXECUTOR_SERVICE_MAX_POOL_SIZE = 100;//this would be 100 * about 6 = 600 threads
	
	public static final long 
			DEFAULT_MAX_FILE_SIZE_BYTES = 1024 * 1024 * 1024,
			DEFAULT_MEMSTORE_FLUSH_SIZE_BYTES = 256 * 1024 * 1024;
	
	public static final byte[] DEFAULT_FAMILY_QUALIFIER = new byte[]{(byte)'a'};
	public static final String DUMMY_COL_NAME = new String(new byte[]{0});
	
	protected Pair<HTablePool,Map<String,Class<PrimaryKey<?>>>> initTables(){
		List<String> tableNames = ListTool.create();
		Map<String,Class<PrimaryKey<?>>> primaryKeyClassByName = MapTool.create();
		@SuppressWarnings("unchecked")
		List<PhysicalNode<?,?>> physicalNodes = router.getNodes().getPhysicalNodesForClient(clientName);
		Map<String,PhysicalNode<?,?>> nodeByTableName = MapTool.createTreeMap();
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
				for(String tableName : IterableTool.nullSafe(tableNames)){
					byte[] tableNameBytes = StringByteTool.getUtf8Bytes(tableName);
					if(createTables && !hBaseAdmin.tableExists(tableName)){
						logger.warn("table " + tableName + " not found, creating it");
						HTableDescriptor hTable = new HTableDescriptor(tableName);
						hTable.setMaxFileSize(DEFAULT_MAX_FILE_SIZE_BYTES);
						hTable.setMemStoreFlushSize(DEFAULT_MEMSTORE_FLUSH_SIZE_BYTES);
						HColumnDescriptor family = new HColumnDescriptor(DEFAULT_FAMILY_QUALIFIER);
						family.setMaxVersions(1);
						family.setBloomFilterType(BloomType.ROW);
						family.setCompressionType(Algorithm.GZ);
						hTable.addFamily(family);
						byte[][] splitPoints = getSplitPoints(nodeByTableName.get(tableName));
						if(splitPoints==null){
							hBaseAdmin.createTable(hTable);
						}else{
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
					options.minPoolSize(DEFAULT_minPoolSize),
					DEFAULT_maxPoolSize);
		} else {
			pool = new HTableExecutorServicePool(hBaseConfig, tableNames,
					EXECUTOR_SERVICE_MAX_POOL_SIZE, primaryKeyClassByName);
		}
		
		return Pair.create(pool,primaryKeyClassByName);
	}

	
	@Override
	public boolean isInitialized(){
		return client!=null;
	}
	
	
	/*
	 * this currently gets ignored on an empty table since there are no regions to split
	 * 
	 * 
	 */
	protected byte[][] getSplitPoints(PhysicalNode<?,?> node){
		DatabeanFieldInfo<?,?,?> fieldInfo = node.getFieldInfo();
		ScatteringPrefix sampleScatteringPrefix = fieldInfo.getSampleScatteringPrefix();
		if(sampleScatteringPrefix==null){ return null; }
		List<byte[]> splitPoints = ListTool.create();
		List<List<Field<?>>> allPrefixes = sampleScatteringPrefix.getAllPossibleScatteringPrefixes();
		int counter = 0;
		for(List<Field<?>> prefixFields : allPrefixes){
			++counter;
			FieldSet<?> prefixFieldSet = new SimpleFieldSet(prefixFields);
			Pair<byte[],byte[]> range = HBaseQueryBuilder.getStartEndBytesForPrefix(prefixFieldSet, false);
			if( ! isSingleEmptyByte(range.getLeft())){
				splitPoints.add(range.getLeft());
			}
//			try{
//				hBaseAdmin.split(StringByteTool.getUtf8Bytes(tableName), range.getLeft());
//			}catch(Exception e){
//				throw new RuntimeException("pre-splitting failed for table:"+tableName, e);
//			}
//			logger.warn("split table "+tableName+" "+counter+"/"+CollectionTool.size(allPrefixes));
		}
		return splitPoints.toArray(new byte[splitPoints.size()][]);
	}
	
	protected boolean isSingleEmptyByte(byte[] bytes){
		if(ArrayTool.length(bytes)!=1){ return false; }
		return bytes[0] == Byte.MIN_VALUE;
	}
}

















