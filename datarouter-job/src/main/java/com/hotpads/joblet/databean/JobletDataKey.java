package com.hotpads.joblet.databean;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63FieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.util.core.number.RandomTool;

public class JobletDataKey extends BasePrimaryKey<JobletDataKey>{

	private Long id;

	public static class FieldKeys{
		public static final UInt63FieldKey id = new UInt63FieldKey("id");
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(
				new UInt63Field(FieldKeys.id, id));
	}


	/******************** construct *************************/

	public JobletDataKey(Long id){
		this.id = id;
	}

	public JobletDataKey(){
		this(RandomTool.nextPositiveLong());
	}


	/******************* get/set *******************************/

	public Long getId(){
		return id;
	}

}