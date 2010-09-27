package com.hotpads.datarouter.client.imp.hbase.factory;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.imp.hbase.HBaseClientImp;
import com.hotpads.datarouter.client.imp.hbase.HTablePool;
import com.hotpads.datarouter.client.type.HBaseClient;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.PropertiesTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.profile.PhaseTimer;

public class HBaseSimpleClientFactory implements HBaseClientFactory{
	Logger logger = Logger.getLogger(getClass());
	
	public static final Long KEEP_ALIVE_TEST_PERIOD_MS = 30*1000L;
	public static final Integer RECONNECT_AFTER_X_FAILURES = 2;
	
	protected DataRouter router;
	protected String clientName;
	protected String configFileLocation;
	protected Properties properties;
	protected HBaseOptions options;
	protected HBaseClient client;
	protected Configuration hbConfig;

	
	public HBaseSimpleClientFactory(
			DataRouter router, String clientName, 
			String configFileLocation){
		this.router = router;
		this.clientName = clientName;
		this.configFileLocation = configFileLocation;
		this.properties = PropertiesTool.ioAndNullSafeFromFile(configFileLocation);
		this.options = new HBaseOptions(properties, clientName);
	}
	
	@Override
	public synchronized HBaseClient getClient(){
		if(client!=null){ return client; }
		logger.warn("activating HBase client "+clientName);
		PhaseTimer timer = new PhaseTimer(clientName);
		
		hbConfig = HBaseConfiguration.create();
		String zkQuorum = options.zookeeperQuorum();
		hbConfig.set(HConstants.ZOOKEEPER_QUORUM, zkQuorum);
		//TODO add custom variables programatically

		//databean config
		HTablePool pool = initTables();
		timer.add("init HTables");
		
		HBaseClientImp newClient = new HBaseClientImp(clientName, options, hbConfig, pool);
		this.client = newClient;
		logger.warn(timer);
		return this.client;
	}
	
	public static final int DEFAULT_minPoolSize = 3;
	public static final byte[] DEFAULT_FAMILY_QUALIFIER = new byte[]{(byte)'a'};
	public static final String DUMMY_COL_NAME = new String(new byte[]{0});
	
	protected HTablePool initTables(){
		List<String> tableNames = ListTool.create();
		@SuppressWarnings("unchecked")
		List<PhysicalNode<?,?>> physicalNodes = router.getNodes().getPhysicalNodesForClient(clientName);
		for(PhysicalNode<?,?> node : physicalNodes){
			tableNames.add(node.getTableName());
		}

		try{
		    HBaseAdmin admin = new HBaseAdmin(hbConfig);
		    
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
					if(createTables && !admin.tableExists(tableName)){
						logger.warn("table " + tableName + " not found, creating it");
						HTableDescriptor hTable = new HTableDescriptor(tableName);
						HColumnDescriptor family = new HColumnDescriptor(DEFAULT_FAMILY_QUALIFIER);
						family.setMaxVersions(1);
						hTable.addFamily(family);
						admin.createTable(hTable);
						logger.warn("created table " + tableName);
					}else if(checkTables){
						if(!admin.tableExists(tableNameBytes)){
							logger.warn("table " + tableName + " not found");
							break;
						}
					}
				}
			}
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		
		HTablePool pool = new HTablePool(hbConfig, 
				tableNames, 
				options.minPoolSize(DEFAULT_minPoolSize));
		
		
		return pool;
	}
	
	
}

















