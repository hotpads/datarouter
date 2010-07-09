package com.hotpads.datarouter.client.imp.hbase;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.node.Nodes;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.profile.PhaseTimer;

public class HBaseClientFactory implements ClientFactory{
	Logger logger = Logger.getLogger(getClass());
	
	public static final String
		hibernate_connection_prefix = "hibernate.connection.",
		provider_class = hibernate_connection_prefix + "provider_class",  //from org.hibernate.cfg.Environment.CONNECTION_PROVIDER
		connectionPoolName = hibernate_connection_prefix + "connectionPoolName";  //any name... SessionFactory simply passes them through
	
	public static final String
		paramConfigLocation = ".configLocation",
		nestedParamSessionFactory = ".param.sessionFactory";
	
	public static final String
		configLocationDefault = "hib-default.cfg.xml";
	
	@Override
	public Client createClient(
			DataRouter router, String clientName, 
			Properties properties, Map<String,Object> params){
		return createFromScratch(router, clientName, properties);
	}
	
	
	@SuppressWarnings("unchecked")
	public HBaseClientImp createFromScratch(
			DataRouter router, String clientName, Properties properties){
		logger.debug("creating HBase client "+clientName);
		PhaseTimer timer = new PhaseTimer(clientName);
		HBaseConfiguration hbConfig = new HBaseConfiguration();
		//TODO add custom variables programatically

		//databean config
		List<byte[]> tableNames = ListTool.create();
		Nodes nodes = router.getNodes();
		List<PhysicalNode> physicalNodes = nodes.getPhysicalNodesForClient(clientName);
		for(PhysicalNode node : physicalNodes){
			tableNames.add(StringByteTool.getByteArray(node.getTableName(), StringByteTool.CHARSET_UTF8));
		}
		HTablePool pool = new HTablePool(hbConfig, tableNames, 3);
		timer.add("init HTables");
		
		HBaseClientImp client = new HBaseClientImp(clientName, pool);
		
		logger.warn(timer);
		
		return client;
	}

	
}