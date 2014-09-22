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
import com.hotpads.datarouter.client.imp.BaseClient;
import com.hotpads.datarouter.client.imp.hbase.factory.HBaseOptions;
import com.hotpads.datarouter.client.imp.hbase.pool.HTablePool;
import com.hotpads.datarouter.client.type.HBaseClient;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.datastructs.MutableString;

public class HBaseClientImp
extends BaseClient
implements HBaseClient{
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	protected String name;
	protected HBaseOptions options;
	protected Configuration hBaseConfiguration;
	protected HBaseAdmin hBaseAdmin;
	protected HTablePool hTablePool;
	protected ExecutorService executorService;
	protected Map<String,Class<PrimaryKey<?>>> primaryKeyClassByName;


	/**************************** constructor **********************************/

	public HBaseClientImp(String name, HBaseOptions options,
			Configuration hBaseConfiguration, HBaseAdmin hBaseAdmin, HTablePool pool,
			Map<String,Class<PrimaryKey<?>>> primaryKeyClassByName){
		this.name = name;
		this.options = options;
		this.hBaseConfiguration = hBaseConfiguration;
		this.hBaseAdmin = hBaseAdmin;
		this.hTablePool = pool;
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
	public String getName(){
		return name;
	}

	@Override
	public ClientType getType(){
		return HBaseClientType.INSTANCE;
	}

	@Override
	public String toString(){
		return this.name;
	}



	/****************************** HBaseClient methods *************************/

	@Override
	public HBaseAdmin getHBaseAdmin(){
		return hBaseAdmin;
	}

	@Override
	public HTable checkOutHTable(String name, MutableString progress){
		return hTablePool.checkOut(name, progress);
	}

	@Override
	public void checkInHTable(HTable hTable, boolean possiblyTarnished){
		hTablePool.checkIn(hTable, possiblyTarnished);
	}

	@Override
	public HTablePool getHTablePool(){
		return hTablePool;
	}

	@Override
	public ExecutorService getExecutorService(){
		return executorService;
	}

	@Override
	public Class<PrimaryKey<?>> getPrimaryKeyClass(String tableName){
		return primaryKeyClassByName.get(tableName);
	}

	public Configuration getHBaseConfiguration(){
		return hBaseConfiguration;
	}

	@Override
	public Integer getTotalPoolSize(){
		return hTablePool.getTotalPoolSize();
	}

	@Override
	public void shutdown(){
		logger.warn("shutting down client:"+name);
//		hTablePool.killOutstandingConnections();
	}
}
