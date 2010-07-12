package com.hotpads.datarouter.client.imp.hbase;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.type.HBaseClient;

public class HBaseClientImp 
implements HBaseClient{

	protected Logger logger = Logger.getLogger(this.getClass());

	protected String name;
	protected HBaseConfiguration hBaseConfiguration;//looks for "hbase-default.xml", and "hbase-site.xml" in the classpath
	protected HTablePool hTablePool;
	
	
	/**************************** constructor **********************************/
	
	public HBaseClientImp(String name, HTablePool pool){
		this.name = name;
		this.hBaseConfiguration = new HBaseConfiguration();
		this.hTablePool = pool;
	}
	
	
	public String getName(){
		return name;
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
	
	
}
