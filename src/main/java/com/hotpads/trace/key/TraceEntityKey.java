package com.hotpads.trace.key;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.key.entity.base.BaseEntityKey;
import com.hotpads.datarouter.storage.key.entity.base.NoOpEntityPartitioner;

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

	public static class TraceEntityPartitioner
	extends NoOpEntityPartitioner<TraceEntityKey>{
	}
	
//	public static class TraceEntityPartitioner implements EntityPartitioner<TraceEntityKey>{
//		@Override
//		public int getNumPartitions(){
//			return 1;
//		}
//		@Override
//		public int getPartition(TraceEntityKey ek){
//			return (int)HashMethods.longDJBHash(ek.getTraceId().toString());
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
