package com.hotpads.datarouter.client.imp.hbase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.hbase.factory.HBaseOptions;
import com.hotpads.datarouter.client.type.HBaseClient;
import com.hotpads.profile.count.collection.Counters;

public class HBaseClientImp 
implements HBaseClient{

	protected Logger logger = Logger.getLogger(this.getClass());

	protected String name;
	protected HBaseOptions options;
	protected HBaseConfiguration hBaseConfiguration;
	protected HTablePool hTablePool;
	protected ExecutorService executorService;
	
	
	/**************************** constructor **********************************/
	
	public HBaseClientImp(String name, HBaseOptions options, 
			HBaseConfiguration hBaseConfiguration, HTablePool pool){
		this.name = name;
		this.options = options;
		this.hBaseConfiguration = hBaseConfiguration;
		this.hTablePool = pool;
		this.executorService = new ThreadPoolExecutor(
				10,
				100,
				60, //irrelevant because our coreSize=maxSize
				TimeUnit.SECONDS,  //irrelevant because our coreSize=maxSize
				new LinkedBlockingQueue<Runnable>(1<<10),
				new ThreadPoolExecutor.AbortPolicy());
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
		Counters.inc("connection getHTable "+this.name);
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
	
	public HBaseConfiguration getHBaseConfiguration(){
		return hBaseConfiguration;
	}
	
	
}
