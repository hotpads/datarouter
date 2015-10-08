package com.hotpads.datarouter.client.imp.memcached.node.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.memcached.client.MemcachedClient;
import com.hotpads.datarouter.client.imp.memcached.node.MemcachedPhysicalNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.datarouter.util.core.DrNumberTool;
import com.hotpads.trace.TraceContext;
import com.hotpads.trace.TraceTool;
import com.hotpads.trace.TracedCallable;

public abstract class MemcachedTask<V> 
extends TracedCallable<V>{
	static Logger logger = LoggerFactory.getLogger(MemcachedTask.class);

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
			DRCounters.incClientNodeCustom(client.getType(), taskName, client.getName(), node.getName());
			TraceTool.startSpan(TraceContext.get(), node.getName()+" "+taskName);
			if(DrNumberTool.nullSafe(numAttempts) > 1){ 
				TraceTool.appendToThreadInfo(TraceContext.get(), "[attempt "+attemptNumOneBased+"/"+numAttempts+"]"); 
			}
			if( ! DrNumberTool.isMax(timeoutMs)){ 
				TraceTool.appendToThreadInfo(TraceContext.get(), "[timeoutMs="+timeoutMs+"]"); 
			}
			return memcachedCall();
		}catch(Exception e){
			throw new DataAccessException(e);
		}finally{
			TraceTool.finishSpan(TraceContext.get());
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
