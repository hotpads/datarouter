package com.hotpads.datarouter.client.imp.hbase;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.imp.BaseClient;
import com.hotpads.datarouter.client.imp.hbase.client.HBaseClient;
import com.hotpads.datarouter.client.imp.hbase.pool.HBaseTablePool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.concurrent.FutureTool;
import com.hotpads.util.core.io.RuntimeIOException;
import com.hotpads.util.datastructs.MutableString;

public class HBaseClientImp
extends BaseClient
implements HBaseClient{
	private static final Logger logger = LoggerFactory.getLogger(HBaseClientImp.class);

	private final Connection connection;
	private final Configuration hbaseConfiguration;
	private final Admin admin;
	private final HBaseTablePool pool;
	private final ExecutorService executorService;
	private final Map<String,Class<? extends PrimaryKey<?>>> primaryKeyClassByName;

	/**************************** constructor **********************************/

	public HBaseClientImp(String name, Connection connection, Admin hbaseAdmin, HBaseTablePool pool,
			Map<String,Class<? extends PrimaryKey<?>>> primaryKeyClassByName, ClientAvailabilitySettings
			clientAvailabilitySettings, ExecutorService executorService){
		super(name, clientAvailabilitySettings);
		this.connection = connection;
		this.hbaseConfiguration = connection.getConfiguration();
		this.admin = hbaseAdmin;
		this.pool = pool;
		this.executorService = executorService;
		this.primaryKeyClassByName = primaryKeyClassByName;
	}

	@Override
	public ClientType getType(){
		return HBaseClientType.INSTANCE;
	}

	/****************************** HBaseClient methods *************************/

	@Override
	public Admin getAdmin(){
		return admin;
	}

	@Override
	public Table getTable(String name){
		try{
			return connection.getTable(TableName.valueOf(name));
		}catch(IOException e){
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public Table checkOutTable(String name, MutableString progress){
		return pool.checkOut(name, progress);
	}

	@Override
	public void checkInTable(Table table, boolean possiblyTarnished){
		pool.checkIn(table, possiblyTarnished);
	}

	@Override
	public HBaseTablePool getHTablePool(){
		return pool;
	}

	@Override
	public ExecutorService getExecutorService(){
		return executorService;
	}

	@Override
	public Class<? extends PrimaryKey<?>> getPrimaryKeyClass(String tableName){
		return primaryKeyClassByName.get(tableName);
	}

	public Configuration getHBaseConfiguration(){
		return hbaseConfiguration;
	}

	@Override
	public void shutdown(){
		logger.warn("shutting down client:"+getName());
		FutureTool.finishAndShutdown(executorService, 5L, TimeUnit.SECONDS);
		pool.shutdown();
	}
}
