package com.hotpads.datarouter.client.imp.hbase;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
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
	private final HBaseAdmin hbaseAdmin;
	private final HTablePool htablePool;
	private final ExecutorService executorService;
	private final Map<String,Class<? extends PrimaryKey<?>>> primaryKeyClassByName;

	/**************************** constructor **********************************/

	public HBaseClientImp(String name, Configuration hbaseConfiguration, HBaseAdmin hbaseAdmin, HTablePool pool,
			Map<String,Class<? extends PrimaryKey<?>>> primaryKeyClassByName, ClientAvailabilitySettings
			clientAvailabilitySettings){
		super(name, clientAvailabilitySettings);
		this.hbaseConfiguration = hbaseConfiguration;
		this.hbaseAdmin = hbaseAdmin;
		this.htablePool = pool;
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
	public HTable checkOutHTable(String tableName, MutableString progress){
		return htablePool.checkOut(tableName, progress);
	}

	@Override
	public void checkInHTable(HTable htable, boolean possiblyTarnished){
		htablePool.checkIn(htable, possiblyTarnished);
	}

	@Override
	public HTablePool getHTablePool(){
		return htablePool;
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
	public Integer getTotalPoolSize(){
		return htablePool.getTotalPoolSize();
	}

	@Override
	public void shutdown(){
		logger.warn("shutting down client:"+getName());
		FutureTool.finishAndShutdown(executorService, 5L, TimeUnit.SECONDS);
		htablePool.shutdown();
	}
}
