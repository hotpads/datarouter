package com.hotpads.datarouter.client.imp.hbase.task;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.type.HBaseClient;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.trace.TraceContext;
import com.hotpads.trace.TracedCallable;
import com.hotpads.util.core.NumberTool;
import com.hotpads.util.datastructs.MutableString;

/*
 * this can be called multiple times by the HBaseMultiAttemp task, so be sure to cleanup state from previous attemps
 * at the beginning of the wrappedCall.  try to keep per-attempt variables inside the scope of wrappedCall()
 */
public abstract class HBaseTask<V> extends TracedCallable<V>{
	static Logger logger = LoggerFactory.getLogger(HBaseTask.class);
	
	protected DatarouterContext drContext;

	//variables for TraceThreads and TraceSpans
	// breaking encapsulation in favor of tracing
	protected String clientName;
	protected String nodeName;
	protected String taskName;
	protected Integer attemptNumOneBased;
	protected Integer numAttempts;
	protected Long timeoutMs;
	
	protected String tableName;
	protected Config config;
	
	protected MutableString progress;

	//subclasses should use this for easy, safe "close()" handling
	protected HBaseClient client;
	protected volatile HTable hTable;
	protected ResultScanner managedResultScanner;
	
	/******************** constructor ****************************/
	
	public HBaseTask(DatarouterContext drContext, HBaseTaskNameParams names, String taskName, Config config){
		super("HBaseTask."+taskName);
		this.drContext = drContext;
		this.clientName = names.getClientName();
		this.nodeName = names.getNodeName();
		this.taskName = taskName;
		//do not set client here.  it is obtained from node in prepClientAndTableEtc(..)
		this.tableName = names.getTableName();
		this.config = Config.nullSafe(config);
		this.progress = new MutableString("");
	}
	
	
	@Override
	public V wrappedCall(){
		clearPreviousAttemptState();
		
		progress.set("starting attemptNumOneBased:"+attemptNumOneBased);
		boolean possiblyTarnishedHTable = false;
		try{
			TraceContext.startSpan(nodeName+" "+taskName);
			recordDetailedTraceInfo();
			
			prepClientAndTableEtc();

			//do this after prepClientAndTableEtc, because client is set in there (null beforehand)
			DRCounters.incSuffixClientNode(client.getType(), taskName, client.getName(), nodeName);
			
			/******************/
			return hbaseCall(); //override this method in subclasses
			/******************/
			
		}catch(Exception e){
			possiblyTarnishedHTable = true;
			logger.warn("rethrowing "+e.getClass().getSimpleName()+" as DataAccessException", e);
			throw new DataAccessException(e);
		}finally{
			progress.set("starting finally block attemptNumOneBased:"+attemptNumOneBased);
			if(managedResultScanner!=null){
				try{
					managedResultScanner.close();
				}catch(Exception e){
					logger.warn("couldn't close ResultScanner", e);
				}
			}

			if(! possiblyTarnishedHTable){
				if(hTable==null){
					logger.warn("not checking in HTable because it's null");
				}
				if(client==null){
					logger.warn("not checking in HTable because client is null");
				}
			}
			client.checkInHTable(hTable, possiblyTarnishedHTable);
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
		client = (HBaseClient)drContext.getClientPool().getClient(clientName);//be sure to get a new client for each attempt/task in case the client was refreshed behind the scenes
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
	
	public String getNodeName(){
		return nodeName;
	}
	
	public String getTaskName(){
		return taskName;
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

	public DatarouterContext getDrContext(){
		return drContext;
	}

	public String getClientName(){
		return clientName;
	}
	
	
}
