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

import com.hotpads.datarouter.client.imp.hbase.HBaseClientImp;
import com.hotpads.datarouter.client.imp.hbase.HTablePool;
import com.hotpads.datarouter.client.type.HBaseClient;
import com.hotpads.datarouter.exception.UnavailableException;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.PropertiesTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.profile.PhaseTimer;

public class HBaseSimpleClientFactory 
implements HBaseClientFactory{
	Logger logger = Logger.getLogger(getClass());
	
	//static caches
	public static Map<String,Configuration> CONFIG_BY_ZK_QUORUM = new ConcurrentHashMap<String,Configuration>();
	public static Map<Configuration,HBaseAdmin> ADMIN_BY_CONFIG = new ConcurrentHashMap<Configuration,HBaseAdmin>();
	
	static final Integer TIMEOUT_MS = 5000;
	
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
			Future<HBaseClient> future = executorService.submit(new Callable<HBaseClient>(){
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
							CONFIG_BY_ZK_QUORUM.put(zkQuorum, hBaseConfig);
							hBaseAdmin = new HBaseAdmin(hBaseConfig);
							ADMIN_BY_CONFIG.put(hBaseConfig, hBaseAdmin);
							//TODO add custom variables programatically
						}
						hBaseAdmin = ADMIN_BY_CONFIG.get(hBaseConfig);
				
						//databean config
						HTablePool pool = initTables();
						timer.add("init HTables");
						
						newClient = new HBaseClientImp(clientName, options, hBaseConfig, hBaseAdmin, pool);
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
			});
			try{
				this.client = future.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
			}catch(InterruptedException e){
				throw new RuntimeException(e);
			}catch(ExecutionException e){
				throw new RuntimeException(e);
			} catch(TimeoutException e) {
				logger.warn("couldn't instantiate client "+clientName+" in "+TIMEOUT_MS+"ms");
				future.cancel(false);
				throw new RuntimeException(e);
			}
		}
		return client;
	}
	
	public static final int 
		DEFAULT_minPoolSize = 1,//these are per-table
		DEFAULT_maxPoolSize = 5;
	
	public static final long 
			DEFAULT_MAX_FILE_SIZE_BYTES = 256 * 1024 * 1024,
			DEFAULT_MEMSTORE_FLUSH_SIZE_BYTES = 256 * 1024 * 1024;
	
	public static final byte[] DEFAULT_FAMILY_QUALIFIER = new byte[]{(byte)'a'};
	public static final String DUMMY_COL_NAME = new String(new byte[]{0});
	
	protected HTablePool initTables(){
		List<String> tableNames = ListTool.create();
		Map<String,Class<PrimaryKey<?>>> primaryKeyClassByName = MapTool.create();
		@SuppressWarnings("unchecked")
		List<PhysicalNode<?,?>> physicalNodes = router.getNodes().getPhysicalNodesForClient(clientName);
		for(PhysicalNode<?,?> node : physicalNodes){
			tableNames.add(node.getTableName());
			primaryKeyClassByName.put(node.getTableName(), (Class<PrimaryKey<?>>)node.getPrimaryKeyType());
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
						hBaseAdmin.createTable(hTable);
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
		
		HTablePool pool = new HTablePool(hBaseConfig, 
				tableNames, 
				options.minPoolSize(DEFAULT_minPoolSize),
				DEFAULT_maxPoolSize,
				primaryKeyClassByName);
		
		
		return pool;
	}

	
	@Override
	public boolean isInitialized(){
		return client!=null;
	}
	
}

















