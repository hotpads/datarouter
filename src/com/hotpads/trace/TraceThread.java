package com.hotpads.trace;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.AccessType;

import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.LongField;
import com.hotpads.datarouter.storage.key.unique.primary.BasePrimaryKey;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;

@Entity
@AccessType("field")
@SuppressWarnings("serial")
public class TraceThread extends BaseDatabean{

	@Id
	protected TraceThreadKey key;

	protected Long parentId;
	@Column(length=255)
	protected String name;
	@Column(length=20)
	protected String serverId;
	protected Long created;
	protected Long queuedDuration;
	protected Long runningDuration;
	
	@Transient 
	protected Long nanoStart;
	protected Long queuedDurationNano;
	protected Long runningDurationNano;
		
	
	/**************************** columns *******************************/
	
	public static final String
		KEY_NAME = "key",
		COL_traceId = "traceId",
		COL_id = "id",
		COL_parentId = "parentId",
		COL_name = "name",
		COL_serverId = "serverId",
		COL_created = "created",
		COL_queuedDuration = "queuedDuration",
		COL_runningDuration = "runningDuration";
	
	
	/*********************** constructor **********************************/
	
	TraceThread(){
	}
	
	public TraceThread(Long traceId, boolean hasParent){
		this.key = new TraceThreadKey(traceId, hasParent);
		this.created = System.currentTimeMillis();
		this.nanoStart = System.nanoTime();
	}
	
	
	/************************** databean **************************************/
	
	@Override
	public TraceThreadKey getKey() {
		return key;
	}
	

	@Embeddable
	public static class TraceThreadKey extends BasePrimaryKey<TraceThread>{
		
		private static Random random = new Random();
		
		protected Long traceId;
		protected Long id;
		
		public static final String
			COL_traceId = "traceId",
			COL_id = "id";
		
		TraceThreadKey(){
			super(TraceThread.class);
		}
		
		public TraceThreadKey(Long traceId, boolean hasParent){
			super(TraceThread.class);
			this.traceId = traceId;
			if( ! hasParent){
				this.id = 0L;
			}else{
				long r = Math.abs(random.nextLong());
				if(Long.MIN_VALUE==r || 0==r){ r = 1; }
				this.id = r;
			}
		}
		
		public TraceThreadKey(Long traceId, Long threadId){
			super(TraceThread.class);
			this.traceId = traceId;
			this.id = threadId;
		}
		
		@Override
		public List<Field<?>> getFields(){
			List<Field<?>> fields = ListTool.create();
			fields.add(new LongField(KEY_NAME, COL_traceId, traceId));
			fields.add(new LongField(KEY_NAME, COL_id, id));
			return fields;
		}

		public Long getTraceId() {
			return traceId;
		}

		public void setTraceId(Long traceId) {
			this.traceId = traceId;
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}
		
	}
	
	/*************************** static ***************************************/
	
	public static Map<TraceThreadKey,TraceThread> getByKey(Iterable<TraceThread> ins){
		Map<TraceThreadKey,TraceThread> outs = MapTool.createHashMap();
		for(TraceThread t : IterableTool.nullSafe(ins)){
			outs.put(t.getKey(), t);
		}
		return outs;
	}
	
	/************************** methods ****************************************/
	
	public void markStart(){
		this.queuedDuration = System.currentTimeMillis() - this.created;
		this.queuedDurationNano = System.nanoTime() - this.nanoStart;
	}

	public void markFinish(){
		this.runningDuration = System.currentTimeMillis() - this.queuedDuration - this.created;
		this.runningDurationNano = System.nanoTime() - this.queuedDurationNano - this.nanoStart;
	}
	
	public Date getTime(){
		return new Date(this.created);
	}
	
	/********************************* get/set ****************************************/


	public void setKey(TraceThreadKey key) {
		this.key = key;
	}


	public String getServerId() {
		return serverId;
	}


	public void setServerId(String serverId) {
		this.serverId = serverId;
	}


	public Long getParentId() {
		return parentId;
	}


	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}



	public Long getCreated() {
		return created;
	}


	public void setCreated(Long created) {
		this.created = created;
	}



	public Long getId() {
		return key.getId();
	}


	public Long getTraceId() {
		return key.getTraceId();
	}


	public void setId(Long id) {
		key.setId(id);
	}


	public void setTraceId(Long traceId) {
		key.setTraceId(traceId);
	}


	public Long getQueuedDuration() {
		return queuedDuration;
	}


	public void setQueuedDuration(Long queuedDuration) {
		this.queuedDuration = queuedDuration;
	}


	public Long getRunningDuration() {
		return runningDuration;
	}


	public void setRunningDuration(Long runningDuration) {
		this.runningDuration = runningDuration;
	}


	public Long getQueuedDurationNano() {
		return queuedDurationNano;
	}


	public void setQueuedDurationNano(Long queuedDurationNano) {
		this.queuedDurationNano = queuedDurationNano;
	}


	public Long getRunningDurationNano() {
		return runningDurationNano;
	}


	public void setRunningDurationNano(Long runningDurationNano) {
		this.runningDurationNano = runningDurationNano;
	}
	
}
