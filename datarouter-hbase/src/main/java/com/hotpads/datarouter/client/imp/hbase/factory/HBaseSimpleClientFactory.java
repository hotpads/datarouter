package com.hotpads.datarouter.client.imp.hbase.factory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.imp.hbase.HBaseStaticContext;
import com.hotpads.datarouter.exception.UnavailableException;
import com.hotpads.datarouter.routing.Datarouter;

public class HBaseSimpleClientFactory
extends BaseHBaseClientFactory{
	private static final Logger logger = LoggerFactory.getLogger(HBaseSimpleClientFactory.class);


	public HBaseSimpleClientFactory(Datarouter datarouter, String clientName, ClientAvailabilitySettings
			clientAvailabilitySettings, ExecutorService executor){
		super(datarouter, clientName, clientAvailabilitySettings, executor);
	}


	@Override
	protected Connection makeConnection(){
		String zkQuorum = options.zookeeperQuorum();
		Configuration hbaseConfig = HBaseStaticContext.CONFIG_BY_ZK_QUORUM.get(zkQuorum);
		if(hbaseConfig==null){
			hbaseConfig = HBaseConfiguration.create();
			hbaseConfig.set(HConstants.ZOOKEEPER_QUORUM, zkQuorum);
		}
		Connection connection;
		Admin admin;
		try{
			connection = ConnectionFactory.createConnection(hbaseConfig);
			admin = connection.getAdmin();
		}catch(IOException e){
			throw new RuntimeException(e);
		}

		if(connection.isClosed()){
			HBaseStaticContext.CONFIG_BY_ZK_QUORUM.remove(zkQuorum);
			HBaseStaticContext.ADMIN_BY_CONFIG.remove(hbaseConfig);
			hbaseConfig = null;
			String log = "couldn't open connection because hBaseAdmin.getConnection().isClosed()";
			logger.warn(log);
			throw new UnavailableException(log);
		}
		HBaseStaticContext.CONFIG_BY_ZK_QUORUM.put(zkQuorum, hbaseConfig);
		HBaseStaticContext.ADMIN_BY_CONFIG.put(hbaseConfig, admin);
		return connection;
	}

}
