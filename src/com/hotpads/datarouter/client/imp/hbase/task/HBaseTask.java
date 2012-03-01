package com.hotpads.datarouter.client.imp.hbase.task;

import junit.framework.Assert;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.imp.hbase.node.HBasePhysicalNode;
import com.hotpads.datarouter.client.type.HBaseClient;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.trace.TraceContext;
import com.hotpads.trace.TracedCallable;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.NumberTool;
import com.hotpads.util.datastructs.MutableString;

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
	
	protected MutableString progress;
	
	//subclasses should use this for easy, safe "close()" handling
	protected ResultScanner managedResultScanner;
	
	
	/******************** constructor ****************************/
	
	public HBaseTask(String taskName, HBasePhysicalNode<?,?> node, Config config){
		super("HBaseTask."+taskName);
		this.taskName = taskName;
		this.node = node;
		this.tableName = node.getTableName();
		this.config = Config.nullSafe(config);
		this.progress = new MutableString("");
		Assert.assertNull(hTable);//previous call should have cleared it in finally block
	}
	
	
	@Override
	public V wrappedCall(){
		progress.set("starting");
		HBaseClient client = null;
		boolean possiblyTarnishedHTable = false;
		try{
			TraceContext.startSpan(node.getName()+" "+taskName);
			recordDetailedTraceInfo();
			client = node.getClient();//be sure to get a new client for each attempt/task in case the client was refreshed behind the scenes
			Assert.assertNotNull(client);
			progress.set("got client");
			Assert.assertNull(hTable);
			hTable = client.checkOutHTable(tableName, progress);
			Assert.assertNotNull(hTable);
			progress.set("got HTable");
			return hbaseCall();
		}catch(Exception e){
			possiblyTarnishedHTable = true;
			throw new DataAccessException(e);
		}finally{
			if(managedResultScanner!=null){
				try{
					managedResultScanner.close();
				}catch(Exception e){
					logger.warn("couldn't close ResultScanner");
					logger.warn(ExceptionTool.getStackTraceAsString(e));
				}
			}
			if(hTable==null){
				logger.warn("not checking in HTable because it's null");
			}else if(client==null){
				logger.warn("not checking in HTable because client is null");
			}else{
				client.checkInHTable(hTable, possiblyTarnishedHTable);
			}
			hTable = null;//reset to null since this HBaseTask will get reused
			TraceContext.finishSpan();
		}
	}
	
	public abstract V hbaseCall() throws Exception;

	
	protected void recordDetailedTraceInfo() {
		if(NumberTool.nullSafe(numAttempts) > 1){ 
			TraceContext.appendToThreadInfo("[attempt "+attemptNumOneBased+"/"+numAttempts+"]"); 
		}
		if( ! NumberTool.isMax(timeoutMs)){ 
			TraceContext.appendToThreadInfo("[timeoutMs="+timeoutMs+"]"); 
		}
	}
	
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
