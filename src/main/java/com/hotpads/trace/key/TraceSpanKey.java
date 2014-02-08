/**
 * 
 */
package com.hotpads.trace.key;

import java.util.List;

import javax.persistence.Embeddable;

import com.hotpads.datarouter.serialize.fielder.BaseFielder;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
@Embeddable
public class TraceSpanKey extends BasePrimaryKey<TraceSpanKey>{

	/****************************** fields ********************************/
	
	//hibernate will create these in the wrong order
	protected Long traceId;
	protected Long threadId;
	protected Integer sequence;
	
	
	public static final String
		COL_traceId = "traceId",
		COL_threadId = "threadId",
		COL_sequence = "sequence";
	
	
	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
				new LongField(COL_traceId, traceId),
				new LongField(COL_threadId, threadId),
				new IntegerField(COL_sequence, sequence));
	}
	
	public static class TraceSpanKeyFielder extends BaseFielder<TraceSpanKey>{
		public TraceSpanKeyFielder(){}
		@Override
		public List<Field<?>> getFields(TraceSpanKey k){
			return FieldTool.createList(
					new LongField(COL_traceId, k.traceId),
					new LongField(COL_threadId, k.threadId),
					new IntegerField(COL_sequence, k.sequence));
		}
	}
	

	/****************************** constructor ********************************/
	
	TraceSpanKey(){
	}
	
	public TraceSpanKey(Long traceId, Long threadId, Integer sequence){
		this.traceId = traceId;
		this.threadId = threadId;
		this.sequence = sequence;
	}
	

	/****************************** get/set ********************************/
	
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