/**
 * 
 */
package com.hotpads.trace.key;

import java.util.List;

import javax.persistence.Embeddable;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.key.primary.base.BaseEntityPrimaryKey;
import com.hotpads.util.core.number.RandomTool;

@SuppressWarnings("serial")
@Embeddable
public class TraceThreadKey extends BaseEntityPrimaryKey<TraceEntityKey,TraceThreadKey>{
	
	
	/***************************** fields ***************************************/
	
	protected Long traceId;
	protected Long id;
	
	
	public static final String
		COL_traceId = "traceId",
		COL_id = "id";

	
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
		return FieldTool.createList(
				new LongField(COL_id, id));
	}

	
	/****************************** constructor ********************************/
	
	TraceThreadKey(){
		super();
	}
	
	public TraceThreadKey(Long traceId, boolean hasParent){
		this.traceId = traceId;
		if( ! hasParent){
			this.id = 0L;
		}else{
			long r = RandomTool.nextPositiveLong();
			this.id = r;
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