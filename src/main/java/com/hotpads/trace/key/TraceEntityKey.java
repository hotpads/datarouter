package com.hotpads.trace.key;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.key.entity.base.BaseEntityKey;

@SuppressWarnings("serial")
public class TraceEntityKey 
extends BaseEntityKey<TraceEntityKey>{

	private Long traceId;

	public static final String
		COL_traceId = "traceId";
	
	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
				new LongField(COL_traceId, traceId));
	}

	public TraceEntityKey(Long traceId){
		this.traceId = traceId;
	}
	
}
