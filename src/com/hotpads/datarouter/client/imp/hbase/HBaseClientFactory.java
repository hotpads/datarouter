package com.hotpads.datarouter.client.imp.hbase;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.profile.PhaseTimer;

public class HBaseClientFactory implements ClientFactory{
	Logger logger = Logger.getLogger(getClass());
	
	protected DataRouter router;
	protected String clientName;
	protected HBaseOptions options;
	
	@Override
	public Client createClient(
			DataRouter router, String clientName, 
			Properties properties, Map<String,Object> params){
		this.router = router;
		this.clientName = clientName;
		this.options = new HBaseOptions(properties, clientName);
		return createFromScratch(router, clientName, properties);
	}
	
	
	public HBaseClientImp createFromScratch(
			DataRouter router, String clientName, Properties properties){
		logger.warn("creating HBase client "+clientName);
		PhaseTimer timer = new PhaseTimer(clientName);
		
		HBaseConfiguration hbConfig = new HBaseConfiguration();
		//TODO add custom variables programatically

		//databean config
		HTablePool pool = initTables(hbConfig);
		timer.add("init HTables");
		
		HBaseClientImp client = new HBaseClientImp(clientName, pool);
		
		logger.warn(timer);
		
		return client;
	}
	
	public static final int DEFAULT_minPoolSize = 3;
	public static final byte[] DEFAULT_FAMILY_QUALIFIER = new byte[]{(byte)'a'};
	
	protected HTablePool initTables(HBaseConfiguration hbConfig){
		List<String> tableNames = ListTool.create();
		@SuppressWarnings("unchecked")
		List<PhysicalNode<?,?>> physicalNodes = router.getNodes().getPhysicalNodesForClient(clientName);
		for(PhysicalNode<?,?> node : physicalNodes){
			tableNames.add(node.getTableName());
		}

		try{
		    HBaseAdmin admin = new HBaseAdmin(hbConfig);
		    
			//manually delete tables here
//		    if(admin.tableExists("TraceThread")){
//		    	admin.disableTable("TraceThread");
//		    	admin.deleteTable("TraceThread");
//		    }
		    if(admin.tableExists("InsertTest3")){
		    	admin.disableTable("InsertTest3");
		    	admin.deleteTable("InsertTest3");
		    }
		
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
				options.getMinPoolSize(DEFAULT_minPoolSize));
		
		
		return pool;
	}

	
}