package com.hotpads.datarouter.client.imp.hbase;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.imp.BaseClient;
import com.hotpads.datarouter.client.imp.hbase.client.HBaseClient;
import com.hotpads.datarouter.client.imp.hbase.pool.HTablePool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.concurrent.FutureTool;
import com.hotpads.util.datastructs.MutableString;

public class HBaseClientImp
extends BaseClient
implements HBaseClient{
	private static final Logger logger = LoggerFactory.getLogger(HBaseClientImp.class);

	private final Configuration hbaseConfiguration;
	private final HTablePool pool;
	private final HBaseAdmin hbaseAdmin;
	private final Admin admin;
	private final ExecutorService executorService;
	private final Map<String,Class<? extends PrimaryKey<?>>> primaryKeyClassByName;

	/**************************** constructor **********************************/

	public HBaseClientImp(String name, Configuration hbaseConfiguration, HTablePool pool, HBaseAdmin hbaseAdmin,
			Admin admin, Map<String,Class<? extends PrimaryKey<?>>> primaryKeyClassByName,
			ClientAvailabilitySettings clientAvailabilitySettings){
		super(name, clientAvailabilitySettings);
		this.hbaseConfiguration = hbaseConfiguration;
		this.pool = pool;
		this.hbaseAdmin = hbaseAdmin;
		this.admin = admin;
		this.executorService = new ThreadPoolExecutor(
				pool.getTotalPoolSize()+10,
				pool.getTotalPoolSize()+10,
				60, //irrelevant because our coreSize=maxSize
				TimeUnit.SECONDS,  //irrelevant because our coreSize=maxSize
				new LinkedBlockingQueue<Runnable>(1),
				new ThreadPoolExecutor.CallerRunsPolicy());
		this.primaryKeyClassByName = primaryKeyClassByName;
	}

	@Override
	public ClientType getType(){
		return HBaseClientType.INSTANCE;
	}

	@Override
	public String toString(){
		return getName();
	}



	/****************************** HBaseClient methods *************************/

	@Override
	public HBaseAdmin getHBaseAdmin(){
		return hbaseAdmin;
	}

	@Override
	public Admin getAdmin(){
		return admin;
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
	public HTablePool getHTablePool(){
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
