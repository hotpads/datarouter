/**
 * 
 */
package com.hotpads.trace.key;

import java.util.List;

import javax.persistence.Embeddable;

import com.hotpads.datarouter.storage.field.BaseField;
import com.hotpads.datarouter.storage.field.imp.IntegerField;
import com.hotpads.datarouter.storage.field.imp.LongField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.trace.TraceSpan;
import com.hotpads.util.core.ListTool;

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
	public List<BaseField<?>> getFields(){
		List<BaseField<?>> fields = ListTool.create();
		fields.add(new LongField(TraceSpan.KEY_key, COL_traceId, traceId));
		fields.add(new LongField(TraceSpan.KEY_key, COL_threadId, threadId));
		fields.add(new IntegerField(TraceSpan.KEY_key, COL_sequence, sequence));
		return fields;
	}
	

	/****************************** constructor ********************************/
	
	TraceSpanKey(){
		super();
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