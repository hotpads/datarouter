/**
 *
 */
package com.hotpads.trace.key;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongFieldKey;
import com.hotpads.datarouter.storage.key.primary.base.BaseEntityPrimaryKey;
import com.hotpads.util.core.number.RandomTool;

@SuppressWarnings("serial")
public class TraceThreadKey extends BaseEntityPrimaryKey<TraceEntityKey,TraceThreadKey>{

	private Long traceId;
	private Long id;

	public static class FieldKeys{
		public static final LongFieldKey id = new LongFieldKey("id");
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
	public TraceThreadKey prefixFromEntityKey(TraceEntityKey entityKey){
		return new TraceThreadKey(entityKey.getTraceId(), null);
	}

	@Override
	public List<Field<?>> getPostEntityKeyFields(){
		return Arrays.asList(new LongField(FieldKeys.id, id));
	}


	/****************************** constructor ********************************/

	TraceThreadKey(){
	}

	public TraceThreadKey(Long traceId, boolean hasParent){
		this.traceId = traceId;
		if( ! hasParent){
			this.id = 0L;
		}else{
			this.id = RandomTool.nextPositiveLong();
		}
	}

	public TraceThreadKey(Long traceId, Long threadId){
		this.traceId = traceId;
		this.id = threadId;
	}


	/****************************** get/set ********************************/

	public Long getTraceId() {
		return traceId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}