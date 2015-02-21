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
import com.hotpads.util.core.collections.Pair;
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
	}
	
	
	@Override
	public V wrappedCall(){
		//clearPreviousAttemptState();
		MutableString progress = new MutableString("");
		progress.set("starting attemptNumOneBased:"+attemptNumOneBased);
		ResultScanner managedResultScanner = null;
		HTable hTable = null;
		HBaseClient client = null;

		boolean possiblyTarnishedHTable = false;

		try{
			TraceContext.startSpan(nodeName+" "+taskName);
			recordDetailedTraceInfo();		
			Pair<HTable, HBaseClient> pair = prepClientAndTableEtc(progress);
			hTable = pair.getLeft();
			client = pair.getRight();
			//do this after prepClientAndTableEtc, because client is set in there (null beforehand)
			DRCounters.incSuffixClientNode(client.getType(), taskName, client.getName(), nodeName);
			
			/******************/
			return hbaseCall(hTable, client, managedResultScanner); //override this method in subclasses
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

			if(hTable == null || client == null){
				if(hTable == null){
					logger.warn("not checking in HTable because it's null and possiblyTarnishedHTable="
							+ possiblyTarnishedHTable);
				}
				if(client == null){
					logger.warn("not checking in HTable because client is null and possiblyTarnishedHTable="
							+ possiblyTarnishedHTable);
				}
			}else{
				client.checkInHTable(hTable, possiblyTarnishedHTable);
			}
			//hTable = null;//reset to null since this HBaseTask will get reused
			TraceContext.finishSpan();
			progress.set("ending finally block attemptNumOneBased:"+attemptNumOneBased);
		}
	}
	
	public abstract V hbaseCall(HTable hTable, HBaseClient client, ResultScanner managedResultScanner) throws Exception;

	
	protected void clearPreviousAttemptState(HTable hTable, HBaseClient client, ResultScanner managedResultScanner){
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
	
	protected Pair<HTable, HBaseClient> prepClientAndTableEtc(MutableString progress){
		//get a fresh copy of the client
		//Preconditions.checkState(client==null);//make sure we cleared this from the previous attempt
		HBaseClient client = (HBaseClient)drContext.getClientPool().getClient(clientName);//be sure to get a new client for each attempt/task in case the client was refreshed behind the scenes
		Preconditions.checkNotNull(client);
		progress.set("got client attemptNumOneBased:"+attemptNumOneBased);
		
		//get a fresh htable
		//Preconditions.checkState(hTable==null);//make sure we cleared this from the previous attempt
		HTable hTable = client.checkOutHTable(tableName, progress);
		Preconditions.checkNotNull(hTable);
		progress.set("got HTable attemptNumOneBased:"+attemptNumOneBased);
		
		hTable.setOperationTimeout((int)Math.min(timeoutMs, Integer.MAX_VALUE));
		
		return new Pair(hTable, client);
		//assert null
		//Preconditions.checkState(managedResultScanner==null);//make sure we cleared this from the previous attempt
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
