package com.hotpads.datarouter.client.imp.hbase.task;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.imp.hbase.node.HBasePhysicalNode;
import com.hotpads.datarouter.client.type.HBaseClient;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.trace.TraceContext;
import com.hotpads.trace.TracedCallable;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.NumberTool;
import com.hotpads.util.datastructs.MutableString;

/*
 * this can be called multiple times by the HBaseMultiAttemp task, so be sure to cleanup state from previous attemps
 * at the beginning of the wrappedCall.  try to keep per-attempt variables inside the scope of wrappedCall()
 */
public abstract class HBaseTask<V> extends TracedCallable<V>{
	static Logger logger = Logger.getLogger(HBaseTask.class);
	
	protected DataRouterContext drContext;

	//variables for TraceThreads and TraceSpans
	// breaking encapsulation in favor of tracing
	protected String taskName;
	protected Integer attemptNumOneBased;
	protected Integer numAttempts;
	protected Long timeoutMs;
	
	protected HBasePhysicalNode<?,?> node;
	protected String tableName;
	protected Config config;
	
	protected MutableString progress;

	//subclasses should use this for easy, safe "close()" handling
	protected HBaseClient client = null;
	protected HTable hTable = null;
	protected ResultScanner managedResultScanner;
	
	/******************** constructor ****************************/
	
	public HBaseTask(DataRouterContext drContext, String taskName, HBasePhysicalNode<?,?> node, Config config){
		super("HBaseTask."+taskName);
		this.drContext = drContext;
		this.taskName = taskName;
		this.node = node;
		this.tableName = node.getTableName();
		this.config = Config.nullSafe(config);
		this.progress = new MutableString("");
	}
	
	@Override
	public V wrappedCall(){
		clearPreviousAttemptState();
		
		progress.set("starting attemptNumOneBased:"+attemptNumOneBased);
		boolean possiblyTarnishedHTable = false;
		try{
			TraceContext.startSpan(node.getName()+" "+taskName);
			recordDetailedTraceInfo();
			
			prepClientAndTableEtc();
			
			/******************/
			return hbaseCall(); //override this method in subclasses
			/******************/
			
		}catch(Exception e){
			possiblyTarnishedHTable = true;
			throw new DataAccessException(e);
		}finally{
			progress.set("starting finally block attemptNumOneBased:"+attemptNumOneBased);
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
			progress.set("ending finally block attemptNumOneBased:"+attemptNumOneBased);
		}
	}
	
	public abstract V hbaseCall() throws Exception;

	
	protected void clearPreviousAttemptState(){
		if(attemptNumOneBased == 1){ return; }//first attempt
		client = null;
		hTable = null;
		managedResultScanner = null;
	}
	
	protected void recordDetailedTraceInfo() {
		if(NumberTool.nullSafe(numAttempts) > 1){ 
			TraceContext.appendToThreadInfo("[attempt "+attemptNumOneBased+"/"+numAttempts+"]"); 
		}
		if( ! NumberTool.isMax(timeoutMs)){ 
			TraceContext.appendToThreadInfo("[timeoutMs="+timeoutMs+"]"); 
		}
	}
	
	protected void prepClientAndTableEtc(){
		//get a fresh copy of the client
		Preconditions.checkState(client==null);//make sure we cleared this from the previous attempt
		client = node.getClient();//be sure to get a new client for each attempt/task in case the client was refreshed behind the scenes
		Preconditions.checkNotNull(client);
		progress.set("got client attemptNumOneBased:"+attemptNumOneBased);
		
		//get a fresh htable
		Preconditions.checkState(hTable==null);//make sure we cleared this from the previous attempt
		hTable = client.checkOutHTable(tableName, progress);
		Preconditions.checkNotNull(hTable);
		progress.set("got HTable attemptNumOneBased:"+attemptNumOneBased);
		
		hTable.setOperationTimeout((int)Math.min(timeoutMs, Integer.MAX_VALUE));

		//assert null
		Preconditions.checkState(managedResultScanner==null);//make sure we cleared this from the previous attempt
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

	public DataRouterContext getDrContext(){
		return drContext;
	}
	
	
}
