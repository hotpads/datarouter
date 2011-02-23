package com.hotpads.datarouter.client.imp.hbase;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.imp.hbase.node.HBasePhysicalNode;
import com.hotpads.datarouter.client.type.HBaseClient;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.trace.TraceContext;
import com.hotpads.trace.TracedCallable;

public abstract class HBaseTask<V> extends TracedCallable<V>{
	static Logger logger = Logger.getLogger(HBaseTask.class);

	protected String taskName;
	protected HBasePhysicalNode<?,?> node;
	protected String tableName;
	protected HBaseClient client;
	protected HTable hTable;
	protected Config config;
	
	public HBaseTask(String taskName, HBasePhysicalNode<?,?> node, Config config){
		super("HBaseTask."+taskName);
		this.taskName = taskName;
		this.node = node;
		this.client = node.getClient();
		this.tableName = node.getTableName();
		this.config = Config.nullSafe(config);
		
	}
	
	@Override
	public V wrappedCall(){
		try{
			TraceContext.startSpan(node.getName()+" "+taskName);
			hTable = client.checkOutHTable(tableName);
			return hbaseCall();
		}catch(Exception e){
			throw new DataAccessException(e);
		}finally{
			if(hTable!=null){
				client.checkInHTable(hTable);
			}
			TraceContext.finishSpan();
		}
	}
	
	public abstract V hbaseCall() throws Exception;

	
	/******************************* get/set ********************************************/
	
	public String getTaskName(){
		return taskName;
	}

	public HBasePhysicalNode<?,?> getNode(){
		return node;
	}

	public String getTableName(){
		return tableName;
	}
	
	
}
