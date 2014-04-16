package com.hotpads.profile.count.databean.key;

import java.util.List;

import javax.persistence.Embeddable;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.profile.count.databean.alert.CounterAlertNotificationType;

@SuppressWarnings("serial")
@Embeddable
public class CounterAlertDestinationKey extends BasePrimaryKey<CounterAlertDestinationKey>{
	public static final int LENGTH_notificationDestination = MySqlColumnType.MAX_LENGTH_VARCHAR; 
	public static final int LENGTH_notificationType = MySqlColumnType.MAX_LENGTH_VARCHAR; 

	private Long counterAlertId;
	private CounterAlertNotificationType counterAlertNotificationType;
	private String notificationDestination;
	
	CounterAlertDestinationKey(){}
	
	public CounterAlertDestinationKey(Long counterAlertId, CounterAlertNotificationType counterAlertNotificationType,
			String notificationDestination){
		this.counterAlertId = counterAlertId;
		this.counterAlertNotificationType = counterAlertNotificationType;
		this.notificationDestination = notificationDestination;
	}
	
	public static class F{
		public static final String 
			counterAlertId = "counterAlertId",
			counterAlertNotificationType = "counterAlertNotificationType",
			notificationDestination = "notificationDestination"
			;
	}
	
	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
				new LongField(F.counterAlertId, counterAlertId),
				new StringEnumField<CounterAlertNotificationType>(CounterAlertNotificationType.class, F.counterAlertNotificationType, counterAlertNotificationType, LENGTH_notificationType),
				new StringField(F.notificationDestination, notificationDestination, LENGTH_notificationDestination)
				);
	}

	public Long getCounterAlertId(){
		return counterAlertId;
	}

	public void setCounterAlertId(Long counterAlertId){
		this.counterAlertId = counterAlertId;
	}

	public CounterAlertNotificationType getCounterAlertNotificationType(){
		return counterAlertNotificationType;
	}

	public void setCounterAlertNotificationType(CounterAlertNotificationType counterAlertNotificationType){
		this.counterAlertNotificationType = counterAlertNotificationType;
	}

	public String getNotificationDestination(){
		return notificationDestination;
	}

	public void setNotificationDestination(String notificationDestination){
		this.notificationDestination = notificationDestination;
	}


}
