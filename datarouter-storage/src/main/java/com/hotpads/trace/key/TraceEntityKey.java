package com.hotpads.trace.key;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.key.entity.base.BaseEntityKey;
import com.hotpads.datarouter.storage.key.entity.base.BaseEntityPartitioner;

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

//	public static class TraceEntityPartitioner
//	extends NoOpEntityPartitioner<TraceEntityKey>{
//	}
	
	public static class TraceEntityPartitioner extends BaseEntityPartitioner<TraceEntityKey>{
		@Override
		public int getNumPartitions(){
			return 16;
		}
		@Override
		public int getPartition(TraceEntityKey ek){
			return (int)(ek.getTraceId() % getNumPartitions());
		}
	}

	
	/****************** construct *******************/
	
	private TraceEntityKey(){//no-arg for reflection
	}
	
	public TraceEntityKey(Long traceId){
		this.traceId = traceId;
	}

	
	/********************** get/set ***************************/
	
	public Long getTraceId(){
		return traceId;
	}
	

}
