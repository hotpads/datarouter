package com.hotpads.datarouter.client.imp.hbase.task;

import java.io.IOException;

import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.ClientTableNodeNames;
import com.hotpads.datarouter.client.imp.hbase.client.HBaseClient;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.datarouter.util.core.DrNumberTool;
import com.hotpads.trace.TracedCallable;
import com.hotpads.trace.TracerThreadLocal;
import com.hotpads.trace.TracerTool;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.datastructs.MutableString;

/*
 * this can be called multiple times by the HBaseMultiAttemp task, so be sure to cleanup state from previous attemps
 * at the beginning of the wrappedCall.  try to keep per-attempt variables inside the scope of wrappedCall()
 */
public abstract class HBaseTask<V> extends TracedCallable<V>{
	static Logger logger = LoggerFactory.getLogger(HBaseTask.class);

	protected Datarouter datarouter;

	//variables for TraceThreads and TraceSpans
	// breaking encapsulation in favor of tracing
	protected final String clientName;
	protected final String nodeName;
	protected final String taskName;
	protected Integer attemptNumOneBased;
	protected Integer numAttempts;
	protected Long timeoutMs;

	protected final String tableName;
	protected final Config config;

	/******************** constructor ****************************/

	public HBaseTask(Datarouter datarouter, ClientTableNodeNames names, String taskName, Config config){
		super("HBaseTask." + taskName);
		this.datarouter = datarouter;
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
		progress.set("starting attemptNumOneBased:" + attemptNumOneBased);
		ResultScanner managedResultScanner = null;
		Table table = null;
		HBaseClient client = null;

		boolean possiblyTarnishedHTable = false;

		try{
			TracerTool.startSpan(TracerThreadLocal.get(), nodeName + " " + taskName);
			recordDetailedTraceInfo();
			Pair<Table,HBaseClient> pair = prepClientAndTableEtc(progress);
			table = pair.getLeft();
			client = pair.getRight();
			//do this after prepClientAndTableEtc, because client is set in there (null beforehand)
			DRCounters.incClientNodeCustom(client.getType(), taskName, client.getName(), nodeName);

			/******************/
			return hbaseCall(table, client, managedResultScanner); //override this method in subclasses
			/******************/

		}catch(Exception e){
			possiblyTarnishedHTable = true;
			logger.warn("rethrowing " + e.getClass().getSimpleName() + " as DataAccessException", e);
			throw new DataAccessException(e);
		}finally{
			progress.set("starting finally block attemptNumOneBased:" + attemptNumOneBased);
			if(managedResultScanner != null){
				try{
					managedResultScanner.close();
				}catch(Exception e){
					logger.warn("couldn't close ResultScanner", e);
				}
			}

			if(table == null || client == null){
				if(table == null){
					logger.warn("not checking in HTable because it's null and possiblyTarnishedHTable="
							+ possiblyTarnishedHTable);
				}
				if(client == null){
					logger.warn("not checking in HTable because client is null and possiblyTarnishedHTable="
							+ possiblyTarnishedHTable);
				}
			}else{
				client.checkInTable(table, possiblyTarnishedHTable);
			}
			//table = null;//reset to null since this HBaseTask will get reused
			TracerTool.finishSpan(TracerThreadLocal.get());
			progress.set("ending finally block attemptNumOneBased:" + attemptNumOneBased);
		}
	}

	public abstract V hbaseCall(Table table, HBaseClient client, ResultScanner managedResultScanner) throws Exception;


//	protected void clearPreviousAttemptState(Table table, HBaseClient client, ResultScanner managedResultScanner){
//		if(attemptNumOneBased == 1){ return; }//first attempt
//		client = null;
//		table = null;
//		managedResultScanner = null;
//	}

	protected void recordDetailedTraceInfo(){
		if(DrNumberTool.nullSafe(numAttempts) > 1){
			TracerTool.appendToThreadInfo(TracerThreadLocal.get(), "[attempt " + attemptNumOneBased + "/" + numAttempts
					+ "]");
		}
		if(!DrNumberTool.isMax(timeoutMs)){
			TracerTool.appendToThreadInfo(TracerThreadLocal.get(), "[timeoutMs=" + timeoutMs + "]");
		}
	}

	protected Pair<Table,HBaseClient> prepClientAndTableEtc(MutableString progress) throws IOException{
		//get a fresh copy of the client
		//Preconditions.checkState(client==null);//make sure we cleared this from the previous attempt
		//Be sure to get a new client for each attempt/task in case the client was refreshed behind the scenes
		HBaseClient client = (HBaseClient)datarouter.getClientPool().getClient(clientName);

		Preconditions.checkNotNull(client);
		progress.set("got client attemptNumOneBased:" + attemptNumOneBased);

		//get a fresh htable
		//Preconditions.checkState(table==null);//make sure we cleared this from the previous attempt
		Table table = client.checkOutTable(tableName, progress);
		Preconditions.checkNotNull(table);
		progress.set("got HTable attemptNumOneBased:" + attemptNumOneBased);

		return new Pair(table, client);
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

	public Datarouter getDatarouter(){
		return datarouter;
	}

	public String getClientName(){
		return clientName;
	}


}
