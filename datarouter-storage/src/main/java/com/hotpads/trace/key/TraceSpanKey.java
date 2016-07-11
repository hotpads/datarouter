/**
 *
 */
package com.hotpads.trace.key;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerField;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerFieldKey;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongFieldKey;
import com.hotpads.datarouter.storage.key.primary.base.BaseEntityPrimaryKey;

@SuppressWarnings("serial")
public class TraceSpanKey extends BaseEntityPrimaryKey<TraceEntityKey,TraceSpanKey>{

	private Long traceId;
	private Long threadId;
	private Integer sequence;

	public static class FieldsKeys{
		public static final LongFieldKey threadId = new LongFieldKey("threadId");
		public static final IntegerFieldKey sequence = new IntegerFieldKey("sequence");
	}

	@Override
	public TraceEntityKey getEntityKey(){
		return new TraceEntityKey(traceId);
	}

	@Override
	public String getEntityKeyName() {
		return null;
	}

	@Override
	public TraceSpanKey prefixFromEntityKey(TraceEntityKey entityKey){
		return new TraceSpanKey(entityKey.getTraceId(), null, null);
	}

	@Override
	public List<Field<?>> getPostEntityKeyFields(){
		return Arrays.asList(
				new LongField(FieldsKeys.threadId, threadId),
				new IntegerField(FieldsKeys.sequence, sequence));
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

	public Long getTraceId(){
		return traceId;
	}

	public Long getThreadId(){
		return threadId;
	}

	public void setThreadId(Long threadId){
		this.threadId = threadId;
	}

	public Integer getSequence(){
		return sequence;
	}

}