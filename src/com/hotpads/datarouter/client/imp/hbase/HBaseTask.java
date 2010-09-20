package com.hotpads.datarouter.client.imp.hbase;

import java.util.concurrent.Callable;

import org.apache.hadoop.hbase.client.HTable;

import com.hotpads.datarouter.client.imp.hbase.node.HBasePhysicalNode;
import com.hotpads.datarouter.client.type.HBaseClient;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.trace.TraceContext;

public abstract class HBaseTask<V> implements Callable<V>{

	protected String taskName;
	protected HBasePhysicalNode<?,?> node;
	protected String tableName;
	protected HBaseClient client;
	protected HTable hTable;
	
	public HBaseTask(String taskName, HBasePhysicalNode<?,?> node){
		this.taskName = taskName;
		this.node = node;
		this.client = node.getClient();
		this.tableName = node.getTableName();
	}
	
	public V call(){
		try{
			TraceContext.startSpan(node.getName()+" "+taskName);
			hTable = client.checkOutHTable(tableName);
			return wrappedCall();
		}catch(Exception e){
			throw new DataAccessException(e);
		}finally{
			TraceContext.finishSpan();
			if(hTable!=null){
				client.checkInHTable(hTable);
			}
		}
	}
	
	public abstract V wrappedCall() throws Exception;
}
