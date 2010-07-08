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
//	protected HTableFactory hTableFactory;
//	protected HTablePool hTablePool;
	
	
	/**************************** constructor **********************************/
	
	public HBaseClientImp(String name){
		this.name = name;
		this.hBaseConfiguration = new HBaseConfiguration();
//		this.hTableFactory = new HTableFactory(this.hBaseConfiguration);
//		this.hTablePool = new HTablePool(this.hTableFactory, 5, 30);//TODO are these variables for the whole pool or each type
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
	public HTable getHTable(byte[] name){
		try{
			return null;//(HTable)this.hTablePool.borrowObject(name);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void returnHTable(HTable hTable){
//		try{
//			this.hTablePool.returnObject(hTable.getTableName(), hTable);
//		}catch(Exception e){
//			throw new RuntimeException(e);
//		}
	}
	
	
}
