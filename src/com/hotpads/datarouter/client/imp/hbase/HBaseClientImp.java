package com.hotpads.datarouter.client.imp.hbase;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.type.HBaseClient;

public class HBaseClientImp 
implements HBaseClient{

	protected Logger logger = Logger.getLogger(this.getClass());

	protected String name;
	protected HBaseOptions options;
	protected HBaseConfiguration hBaseConfiguration;
	protected HTablePool hTablePool;
	
	
	/**************************** constructor **********************************/
	
	public HBaseClientImp(String name, HBaseOptions options, 
			HBaseConfiguration hBaseConfiguration, HTablePool pool){
		this.name = name;
		this.options = options;
		this.hBaseConfiguration = hBaseConfiguration;
		this.hTablePool = pool;
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
	
	public HBaseConfiguration getHBaseConfiguration(){
		return hBaseConfiguration;
	}
	
}
