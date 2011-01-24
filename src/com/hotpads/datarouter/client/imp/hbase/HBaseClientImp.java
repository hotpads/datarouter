package com.hotpads.datarouter.client.imp.hbase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.BaseClient;
import com.hotpads.datarouter.client.imp.hbase.factory.HBaseOptions;
import com.hotpads.datarouter.client.type.HBaseClient;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class HBaseClientImp 
extends BaseClient
implements HBaseClient{

	protected Logger logger = Logger.getLogger(this.getClass());

	protected String name;
	protected HBaseOptions options;
	protected Configuration hBaseConfiguration;
	protected HTablePool hTablePool;
	protected ExecutorService executorService;
	
	
	/**************************** constructor **********************************/
	
	public HBaseClientImp(String name, HBaseOptions options, 
			Configuration hBaseConfiguration, HTablePool pool){
		this.name = name;
		this.options = options;
		this.hBaseConfiguration = hBaseConfiguration;
		this.hTablePool = pool;
		this.executorService = Executors.newCachedThreadPool();
//			new ThreadPoolExecutor(
//				100,
//				100,
//				60, //irrelevant because our coreSize=maxSize
//				TimeUnit.SECONDS,  //irrelevant because our coreSize=maxSize
//				new LinkedBlockingQueue<Runnable>(1<<10),
//				new ThreadPoolExecutor.AbortPolicy());
	}
	
	@Override
	public String getName(){
		return name;
	}
	
	@Override
	public ClientType getType(){
		return ClientType.hbase;
	}
	
	@Override
	public String toString(){
		return this.name;
	}

	
	
	/****************************** HBaseClient methods *************************/
	
	@Override
	public HTable checkOutHTable(String name){
		return hTablePool.checkOut(name);
	}
	
	@Override
	public void checkInHTable(HTable hTable){
		hTablePool.checkIn(hTable);
	}

	@Override
	public ExecutorService getExecutorService(){
		return executorService;
	}

	@Override
	public Class<PrimaryKey<?>> getPrimaryKeyClass(String tableName){
		return hTablePool.getPrimaryKeyClass(tableName);
	}
	
	public Configuration getHBaseConfiguration(){
		return hBaseConfiguration;
	}
}
