package com.hotpads.config.job.databean;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.util.core.number.RandomTool;

@SuppressWarnings("serial")
public class JobletDataKey extends BasePrimaryKey<JobletDataKey>{
	
	private Long id;
	
	public static class F{
		public static final String
			id = "id";
	}

	@Override
	public List<Field<?>> getFields() {
		return FieldTool.createList(
				new UInt63Field(F.id, id));
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