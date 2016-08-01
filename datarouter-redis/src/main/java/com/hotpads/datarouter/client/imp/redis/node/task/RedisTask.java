package com.hotpads.datarouter.client.imp.redis.node.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.redis.client.RedisClient;
import com.hotpads.datarouter.client.imp.redis.node.RedisPhysicalNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.datarouter.util.core.DrNumberTool;
import com.hotpads.trace.TracedCallable;
import com.hotpads.trace.TracerThreadLocal;
import com.hotpads.trace.TracerTool;

public abstract class RedisTask<V> extends TracedCallable<V>{

	private static Logger logger = LoggerFactory.getLogger(RedisTask.class);

	//variables for TraceThreads and TraceSpans
	// breaking encapsulation in favor of tracing
	protected String taskName;
	protected Integer attemptNumOneBased;
	protected Integer numAttempts;
	protected Long timeoutMs;

	protected RedisPhysicalNode<?,?> node;
	protected String tableName;
	protected RedisClient client;
	protected redis.clients.jedis.Jedis jedisClient;
	protected Config config;

	/** constructor **********************************************************/

	public RedisTask(String taskName, RedisPhysicalNode<?,?> node, Config config){
		super("MemcachedTask."+taskName);
		this.taskName = taskName;
		this.node = node;
		this.client = node.getClient();
		this.jedisClient = client.getJedisClient();
		this.tableName = node.getTableName();
		this.config = Config.nullSafe(config);
	}

	@Override
	public V wrappedCall(){
		try{
			DRCounters.incClientNodeCustom(client.getType(), taskName, client.getName(), node.getName());
			TracerTool.startSpan(TracerThreadLocal.get(), node.getName()+" "+taskName);
			if(DrNumberTool.nullSafe(numAttempts) > 1){
				TracerTool.appendToThreadInfo(TracerThreadLocal.get(), "[attempt " + attemptNumOneBased + "/"
						+ numAttempts + "]");
			}
			if(! DrNumberTool.isMax(timeoutMs)){
				TracerTool.appendToThreadInfo(TracerThreadLocal.get(), "[timeoutMs="+timeoutMs+"]");
			}
			return redisCall();
		}catch(Exception e){
			throw new DataAccessException(e);
		}finally{
			TracerTool.finishSpan(TracerThreadLocal.get());
		}
	}

	public abstract V redisCall() throws Exception;

	/** get/set **************************************************************/

	public String getTaskName(){
		return taskName;
	}

	public RedisPhysicalNode<?,?> getNode(){
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