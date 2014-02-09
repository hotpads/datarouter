package com.hotpads.datarouter.client.imp.memcached.node.task;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.memcached.MemcachedClient;
import com.hotpads.datarouter.client.imp.memcached.node.MemcachedPhysicalNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.trace.TraceContext;
import com.hotpads.trace.TracedCallable;
import com.hotpads.util.core.NumberTool;

public abstract class MemcachedTask<V> 
extends TracedCallable<V>{
	static Logger logger = Logger.getLogger(MemcachedTask.class);

	//variables for TraceThreads and TraceSpans
	// breaking encapsulation in favor of tracing
	protected String taskName;
	protected Integer attemptNumOneBased;
	protected Integer numAttempts;
	protected Long timeoutMs;
	
	protected MemcachedPhysicalNode<?,?> node;
	protected String tableName;
	protected MemcachedClient client;
	protected net.spy.memcached.MemcachedClient spyClient;
	protected Config config;
	
	public MemcachedTask(String taskName, MemcachedPhysicalNode<?,?> node, Config config){
		super("MemcachedTask."+taskName);
		this.taskName = taskName;
		this.node = node;
		this.client = node.getClient();
		this.spyClient = client.getSpyClient();
		this.tableName = node.getTableName();
		this.config = Config.nullSafe(config);
		
	}
	
	@Override
	public V wrappedCall(){
		try{
			DRCounters.incSuffixClientNode(client.getType(), taskName, client.getName(), node.getName());
			TraceContext.startSpan(node.getName()+" "+taskName);
			if(NumberTool.nullSafe(numAttempts) > 1){ 
				TraceContext.appendToThreadInfo("[attempt "+attemptNumOneBased+"/"+numAttempts+"]"); 
			}
			if( ! NumberTool.isMax(timeoutMs)){ 
				TraceContext.appendToThreadInfo("[timeoutMs="+timeoutMs+"]"); 
			}
			return memcachedCall();
		}catch(Exception e){
			throw new DataAccessException(e);
		}finally{
			TraceContext.finishSpan();
		}
	}
	
	public abstract V memcachedCall() throws Exception;

	
	/******************************* get/set ********************************************/
	
	public String getTaskName(){
		return taskName;
	}

	public MemcachedPhysicalNode<?,?> getNode(){
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
