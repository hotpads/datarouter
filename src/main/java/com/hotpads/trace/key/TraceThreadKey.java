/**
 * 
 */
package com.hotpads.trace.key;

import java.util.List;
import java.util.Random;

import javax.persistence.Embeddable;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
@Embeddable
public class TraceThreadKey extends BasePrimaryKey<TraceThreadKey>{
	
	private static Random random = new Random();
	
	
	/***************************** fields ***************************************/
	
	protected Long traceId;
	protected Long id;
	
	
	public static final String
		COL_traceId = "traceId",
		COL_id = "id";

	
	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
				new LongField(COL_traceId, traceId),
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
			long r = Math.abs(random.nextLong());
			if(Long.MIN_VALUE==r || 0==r){ r = 1; }
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