package com.hotpads.datarouter.client.imp.hbase;

import java.util.concurrent.Callable;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.imp.hbase.node.HBasePhysicalNode;
import com.hotpads.datarouter.client.type.HBaseClient;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.trace.TraceContext;

public abstract class HBaseTask<V> implements Callable<V>{
	static Logger logger = Logger.getLogger(HBaseTask.class);

	protected String taskName;
	protected HBasePhysicalNode<?,?> node;
	protected String tableName;
	protected HBaseClient client;
	protected HTable hTable;
	protected Config config;
	
	public HBaseTask(String taskName, HBasePhysicalNode<?,?> node, Config config){
		this.taskName = taskName;
		this.node = node;
		this.client = node.getClient();
		this.tableName = node.getTableName();
		this.config = Config.nullSafe(config);
		
	}
	
	public V call(){
		try{
			TraceContext.startSpan(node.getName()+" "+taskName);
			hTable = client.checkOutHTable(tableName);
			return wrappedCall();
		}catch(Exception e){
			throw new DataAccessException(e);
		}finally{
			if(hTable!=null){
				client.checkInHTable(hTable);
			}
			TraceContext.finishSpan();
		}
	}
	
	public abstract V wrappedCall() throws Exception;
}
