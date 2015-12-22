package com.hotpads.profile.count.databean.key;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
public class CounterAlertKey extends BasePrimaryKey<CounterAlertKey>{
	private Long counterAlertId;
	
	CounterAlertKey(){}

	public CounterAlertKey(Long counterAlertId){
		this.counterAlertId = counterAlertId;
	}

	public static class F{
		public static final String 
		counterAlertId = "counterAlertId";
	}
	
	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
				new UInt63Field(F.counterAlertId, counterAlertId));
	}

	public Long getCounterAlertId(){
		return counterAlertId;
	}

	public void setCounterAlertId(Long id){
		this.counterAlertId = id;
	}
}
