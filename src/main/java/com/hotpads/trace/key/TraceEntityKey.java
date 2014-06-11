package com.hotpads.trace.key;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.key.entity.base.BaseEntityKey;

@SuppressWarnings("serial")
public class TraceEntityKey 
extends BaseEntityKey<TraceEntityKey>{

	/************* fields *************************/
	
	private Long traceId;

	
	public static class Fields{
		public static final String
			traceId = "traceId";
	}
	
	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
				new LongField(Fields.traceId, traceId));
	}
	
//	public static class TraceEntityKeyFielder implements Fielder<TraceEntityKey>{
//		@Override
//		public List<Field<?>> getFields(TraceEntityKey k){
//			return k.getFields();
//		}
//	}

	
	/****************** construct *******************/
	
	public TraceEntityKey(Long traceId){
		this.traceId = traceId;
	}

	
	/********************** get/set ***************************/
	
	public Long getTraceId(){
		return traceId;
	}
	

}
