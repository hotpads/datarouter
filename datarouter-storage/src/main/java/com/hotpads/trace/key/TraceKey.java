/**
 * 
 */
package com.hotpads.trace.key;

import java.util.List;
import java.util.Random;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.key.primary.base.BaseEntityPrimaryKey;
import com.hotpads.util.core.number.RandomTool;

@SuppressWarnings("serial")
public class TraceKey extends BaseEntityPrimaryKey<TraceEntityKey,TraceKey>{

	private static Random random = new Random();
	
	private Long id;

	public static class Fields{
		public static final String
			id = "id";
	}
	
//	//fielder for entity case
//	//this may not actually matter since column names
//	public static class TraceKeyEntityFielder implements PrimaryKeyFielder<TraceKey>{
//		@Override
//		public List<Field<?>> getFields(TraceKey k){
//			//3 options here
////			return new TraceEntityKeyFielder().getFields(k.getEntityKey());
////			return k.getEntityKey().getFields();
//			return k.getEntityKeyFields();
//		}
//		@Override
//		public boolean isEntity(){
//			return true;
//		}
//	}
//	
//	//fielder for non-entity case: override the standard traceId col name
//	public static class TraceKeyFielder implements PrimaryKeyFielder<TraceKey>{
//		@Override
//		public List<Field<?>> getFields(TraceKey k){
//			return FieldTool.createList(
//					new LongField(Fields.id, k.id));
//		}
//		@Override
//		public boolean isEntity(){
//			return false;
//		}
//	}
	
	/********************** entity ************************/
	
	@Override
	public TraceEntityKey getEntityKey(){
		return new TraceEntityKey(id);
	}

	@Override
	public String getEntityKeyName() {
		return null;
	}

	@Override
	public TraceKey prefixFromEntityKey(TraceEntityKey entityKey){
		return new TraceKey(entityKey.getTraceId());
	}
	
	@Override//special override because TraceEntityKey calls the column "traceId"
	public List<Field<?>> getEntityKeyFields(){
		return FieldTool.createList(
				new LongField(Fields.id, id));
	}
	
	@Override
	public List<Field<?>> getPostEntityKeyFields(){
		return FieldTool.createList();
	}
	
	
	/**************** construct ************************/

	public TraceKey(){//remember no-arg is required
		long r = RandomTool.nextPositiveLong();
		this.id = r;
	}
	
	public TraceKey(Long id){
		this.id = id;
	}
	
	
	/*************** get/set *************************/

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
}