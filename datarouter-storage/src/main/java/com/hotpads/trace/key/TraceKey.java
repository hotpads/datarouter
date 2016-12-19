/**
 *
 */
package com.hotpads.trace.key;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongFieldKey;
import com.hotpads.datarouter.storage.key.primary.base.BaseEntityPrimaryKey;
import com.hotpads.util.core.number.RandomTool;

public class TraceKey extends BaseEntityPrimaryKey<TraceEntityKey,TraceKey>{

	private Long id;

	public static class FieldsKeys{
		public static final LongFieldKey id = new LongFieldKey("id");
	}

	/********************** entity ************************/

	@Override
	public TraceEntityKey getEntityKey(){
		return new TraceEntityKey(id);
	}

	@Override
	public String getEntityKeyName(){
		return null;
	}

	@Override
	public TraceKey prefixFromEntityKey(TraceEntityKey entityKey){
		return new TraceKey(entityKey.getTraceId());
	}

	@Override // special override because TraceEntityKey calls the column "traceId"
	public List<Field<?>> getEntityKeyFields(){
		return Arrays.asList(new LongField(FieldsKeys.id, id));
	}

	@Override
	public List<Field<?>> getPostEntityKeyFields(){
		return Collections.emptyList();
	}

	/**************** construct ************************/

	public TraceKey(){// required no-arg
		this.id = RandomTool.nextPositiveLong();
	}

	public TraceKey(Long id){
		this.id = id;
	}

	/*************** get/set *************************/

	public Long getId(){
		return id;
	}

	public void setId(Long id){
		this.id = id;
	}

}