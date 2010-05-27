package com.hotpads.trace;

import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.AccessType;

import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.IntegerField;
import com.hotpads.datarouter.storage.field.imp.LongField;
import com.hotpads.datarouter.storage.key.unique.primary.BasePrimaryKey;
import com.hotpads.trace.TraceThread.TraceThreadKey;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.NumberTool;

@Entity
@AccessType("field")
@SuppressWarnings("serial")
public class TraceSpan extends BaseDatabean{

	@Id
	protected TraceSpanKey key;
	protected Integer parentSequence;
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
		COL_parentSequence = "parentSequence",
		COL_name = "name",
		COL_created = "created",
		COL_duration = "duration";
	
	
	/*********************** constructor **********************************/
	
	TraceSpan(){
	}
	
	public TraceSpan(Long traceId, Long threadId, Integer sequence, Integer parentSequence){
		this.key = new TraceSpanKey(traceId, threadId, sequence);
		this.parentSequence = parentSequence;
		this.created = System.currentTimeMillis();
		this.nanoStart = System.nanoTime();
	}
	
	
	/************************** databean **************************************/
	
	@Override
	public TraceSpanKey getKey() {
		return key;
	}
	
	public TraceThreadKey getThreadKey(){
		return new TraceThreadKey(this.getTraceId(), this.getThreadId());
	}
	

	@Embeddable
	public static class TraceSpanKey extends BasePrimaryKey<TraceSpan>{
		
		//hibernate will create these in the wrong order
		protected Long traceId;
		protected Long threadId;
		protected Integer sequence;

		public static final String
			COL_traceId = "traceId",
			COL_threadId = "threadId",
			COL_sequence = "sequence";
		

		/****************************** constructor ********************************/
		
		TraceSpanKey(){
			super(TraceSpan.class);
		}
		
		public TraceSpanKey(Long traceId, Long threadId, Integer sequence){
			super(TraceSpan.class);
			this.traceId = traceId;
			this.threadId = threadId;
			this.sequence = sequence;
		}
		
		@Override
		public List<Field<?>> getFields(){
			List<Field<?>> fields = ListTool.create();
			fields.add(new LongField(KEY_key, COL_traceId, traceId));
			fields.add(new LongField(KEY_key, COL_threadId, threadId));
			fields.add(new IntegerField(KEY_key, COL_sequence, sequence));
			return fields;
		}

		public Long getTraceId() {
			return traceId;
		}
		public void setTraceId(Long traceId) {
			this.traceId = traceId;
		}
		public Long getThreadId() {
			return threadId;
		}
		public void setThreadId(Long threadId) {
			this.threadId = threadId;
		}
		public Integer getSequence() {
			return sequence;
		}
		public void setSequence(Integer sequence) {
			this.sequence = sequence;
		}
		
		
	}
	
	/****************************** static ****************************************/
	
	public static SortedMap<TraceThreadKey,SortedSet<TraceSpan>> getByThreadKey(
			Iterable<TraceSpan> spans){
		SortedMap<TraceThreadKey,SortedSet<TraceSpan>> out = MapTool.createTreeMap();
		for(TraceSpan s : IterableTool.nullSafe(spans)){
			TraceThreadKey threadKey = s.getThreadKey();
			if(out.get(threadKey)==null){ out.put(threadKey, new TreeSet<TraceSpan>()); }
			out.get(threadKey).add(s);
		}
		return out;
	}
	
	public static Long totalDurationOfNonChildren(Iterable<TraceSpan> spans){
		Long sum = 0L;
		for(TraceSpan s : IterableTool.nullSafe(spans)){
			if(s.isTopLevel()){
				sum += NumberTool.nullSafeLong(s.getDuration(), 0L);
			}
		}
		return sum;
	}
	
	/******************************** methods *************************************/

	public void markFinish(){
		this.duration = System.currentTimeMillis() - this.created;
		this.durationNano = System.nanoTime() - this.nanoStart;
	}
	
	public boolean isTopLevel(){
		return this.parentSequence==null;
	}
	
	/********************************* get/set ****************************************/


	public void setKey(TraceSpanKey key) {
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



	public Integer getSequence() {
		return key.getSequence();
	}


	public Long getThreadId() {
		return key.getThreadId();
	}

	public Long getTraceId(){
		return key.getTraceId();
	}

	public void setSequence(Integer sequence){
		key.setSequence(sequence);
	}

	public void setThreadId(Long threadId){
		key.setThreadId(threadId);
	}

	public void setTraceId(Long traceId){
		key.setTraceId(traceId);
	}

	public Long getDuration(){
		return duration;
	}

	public void setDuration(Long duration){
		this.duration = duration;
	}

	public Long getDurationNano(){
		return durationNano;
	}

	public void setDurationNano(Long durationNano){
		this.durationNano = durationNano;
	}

	public Integer getParentSequence(){
		return parentSequence;
	}

	public void setParentSequence(Integer parentSequence){
		this.parentSequence = parentSequence;
	}

	
}
