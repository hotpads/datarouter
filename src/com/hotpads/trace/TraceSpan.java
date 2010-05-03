package com.hotpads.trace;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.AccessType;

import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.BaseKey;
import com.hotpads.util.core.ListTool;

@Entity
@AccessType("field")
@SuppressWarnings("serial")
public class TraceSpan extends BaseDatabean{

	@Id
	protected TraceCallKey key;
	@Column(length=255)
	protected String name;
	protected Long created;
	protected Long duration;

	@Transient 
	protected Long nanoStart;
	protected Long durationNano;
		
	
	/**************************** columns *******************************/
	
	public static final String
		KEY_key = "key",
		COL_name = "name",
		COL_created = "created",
		COL_duration = "duration";
	
	
	/*********************** constructor **********************************/
	
	public TraceSpan(Long traceId, Long threadId, Integer sequence){
		this.key = new TraceCallKey(traceId, threadId, sequence);
		this.created = System.currentTimeMillis();
		this.nanoStart = System.nanoTime();
	}
	
	
	/************************** databean **************************************/
	
	@Override
	public TraceCallKey getKey() {
		return key;
	}
	
	
	@Embeddable
	public static class TraceCallKey extends BaseKey<TraceSpan>{
		
		//hibernate will create these in the wrong order
		protected Long traceId;
		protected Long threadId;
		protected Integer sequence;

		public static final String
			COL_traceId = "traceId",
			COL_threadId = "threadId",
			COL_sequence = "sequence";
		
		public TraceCallKey(Long traceId, Long threadId, Integer sequence){
			super(TraceSpan.class);
			this.traceId = traceId;
			this.threadId = threadId;
			this.sequence = sequence;
		}
		
		@Override
		public List<Field> getFields(){
			return ListTool.create(
					new Field(KEY_key, COL_traceId, traceId),
					new Field(KEY_key, COL_threadId, threadId),
					new Field(KEY_key, COL_sequence, sequence));
		}
	}
	
	/******************************** methods *************************************/

	public void markFinish(){
		this.duration = System.currentTimeMillis() - this.created;
		this.durationNano = System.nanoTime() - this.nanoStart;
	}
	
	/********************************* get/set ****************************************/


	public void setKey(TraceCallKey key) {
		this.key = key;
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


	public Long getDuration() {
		return durationNano;
	}


	public void setDuration(Long duration) {
		this.durationNano = duration;
	}


	public Long getDurationNano() {
		return durationNano;
	}


	public void setDurationNano(Long durationNano) {
		this.durationNano = durationNano;
	}


	
}
