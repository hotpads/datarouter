package com.hotpads.profile.count.databean.key;

import java.util.List;

import javax.persistence.Embeddable;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
@Embeddable
public class CounterAlertDestinationKey extends BasePrimaryKey<CounterAlertDestinationKey>{
	private Long counterAlertId;
	
	public CounterAlertDestinationKey(){}
	
	public CounterAlertDestinationKey(Long counterAlertId){
		this.counterAlertId = counterAlertId;
	}
	
	public static class F{
		public static final String 
			counterAlertId = "counterAlertId";
	}
	
	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
				new LongField(F.counterAlertId, counterAlertId));
	}

	public Long getCounterAlertId(){
		return counterAlertId;
	}

	public void setCounterAlertId(Long counterAlertId){
		this.counterAlertId = counterAlertId;
	}


}
