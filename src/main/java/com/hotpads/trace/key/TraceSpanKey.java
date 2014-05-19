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
import com.hotpads.datarouter.storage.key.primary.base.BaseEntityPrimaryKey;

@SuppressWarnings("serial")
@Embeddable
public class TraceSpanKey extends BaseEntityPrimaryKey<TraceEntityKey,TraceSpanKey>{

	/****************************** fields ********************************/
	
	//hibernate will create these in the wrong order
	private Long traceId;
	private Long threadId;
	private Integer sequence;
	
	public static class Fields{
		public static final String
			threadId = "threadId",
			sequence = "sequence";
	}

	
	@Override
	public TraceEntityKey getEntityKey(){
		return new TraceEntityKey(traceId);
	}
	
	@Override
	public List<Field<?>> getPostEntityKeyFields(){
		return FieldTool.createList(
				new LongField(Fields.threadId, threadId),
				new IntegerField(Fields.sequence, sequence));
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