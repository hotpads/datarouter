package com.hotpads.datarouter.client.imp.hbase.task;

import junit.framework.Assert;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.imp.hbase.node.HBasePhysicalNode;
import com.hotpads.datarouter.client.type.HBaseClient;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.trace.TraceContext;
import com.hotpads.trace.TracedCallable;
import com.hotpads.util.core.NumberTool;

public abstract class HBaseTask<V> extends TracedCallable<V>{
	static Logger logger = Logger.getLogger(HBaseTask.class);

	//variables for TraceThreads and TraceSpans
	// breaking encapsulation in favor of tracing
	protected String taskName;
	protected Integer attemptNumOneBased;
	protected Integer numAttempts;
	protected Long timeoutMs;
	
	protected HBasePhysicalNode<?,?> node;
	protected String tableName;
	protected Config config;
	protected HTable hTable;
	
	public HBaseTask(String taskName, HBasePhysicalNode<?,?> node, Config config){
		super("HBaseTask."+taskName);
		this.taskName = taskName;
		this.node = node;
		this.tableName = node.getTableName();
		this.config = Config.nullSafe(config);
		Assert.assertNull(hTable);//previous call should have cleared it in finally block
	}
	
	@Override
	public V wrappedCall(){
		HBaseClient client = null;
		boolean possiblyTarnishedHTable = false;
		try{
			TraceContext.startSpan(node.getName()+" "+taskName);
			if(NumberTool.nullSafe(numAttempts) > 1){ 
				TraceContext.appendToThreadInfo("[attempt "+attemptNumOneBased+"/"+numAttempts+"]"); 
			}
			if( ! NumberTool.isMax(timeoutMs)){ 
				TraceContext.appendToThreadInfo("[timeoutMs="+timeoutMs+"]"); 
			}
			client = node.getClient();//be sure to get a new client for each attempt/task in case the client was refreshed behind the scenes
//			logger.warn("got client "+System.identityHashCode(client));
			hTable = client.checkOutHTable(tableName);
			return hbaseCall();
		}catch(Exception e){
			possiblyTarnishedHTable = true;
			throw new DataAccessException(e);
		}finally{
			if(hTable!=null && client!=null){
				client.checkInHTable(hTable, possiblyTarnishedHTable);
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

	public Integer getAttemptNumOneBased(){
		return attemptNumOneBased;
	}

	public void setAttemptNumOneBased(Integer attemptNumOneBased){
		this.attemptNumOneBased = attemptNumOneBased;
	}

	public Integer getNumAttempts(){
		return numAttempts;
	}

	public void setNumAttempts(Integer numAttempts){
		this.numAttempts = numAttempts;
	}

	public Long getTimeoutMs(){
		return timeoutMs;
	}

	public void setTimeoutMs(Long timeoutMs){
		this.timeoutMs = timeoutMs;
	}
	
	
}
