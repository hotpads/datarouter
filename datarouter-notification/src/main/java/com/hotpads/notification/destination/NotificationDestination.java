package com.hotpads.notification.destination;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.DateFieldKey;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.field.imp.comparable.BooleanField;
import com.hotpads.datarouter.storage.field.imp.comparable.BooleanFieldKey;

/* CREATE SCRIPT
com.hotpads.notification.destination.NotificationDestination{
  PK{
    StringField token
    StringEnumField<NotificationDestinationAppEnum> app,
    StringField deviceId,
  }
  StringField deviceName,
  BooleanField active

}
*/

public class NotificationDestination extends BaseDatabean<NotificationDestinationKey,NotificationDestination>{

	private NotificationDestinationKey key;

	private String deviceName;
	private Boolean active;
	private Date created;

	public static class FieldKeys{
		public static final StringFieldKey deviceName = new StringFieldKey("deviceName")
				.withSize(MySqlColumnType.INT_LENGTH_LONGTEXT);
		public static final BooleanFieldKey active = new BooleanFieldKey("active");
		public static final DateFieldKey created = new DateFieldKey("created");
	}

	public static class NotificationDestinationFielder
	extends BaseDatabeanFielder<NotificationDestinationKey,NotificationDestination>{

		public NotificationDestinationFielder(){
			super(NotificationDestinationKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(NotificationDestination databean){
			return Arrays.asList(
					new StringField(FieldKeys.deviceName, databean.deviceName),
					new BooleanField(FieldKeys.active, databean.active),
					new DateField(FieldKeys.created, databean.created));
		}

	}

	public NotificationDestination(){
		this.key = new NotificationDestinationKey();
	}

	public NotificationDestination(String token, String app, String deviceId){
		this.key = new NotificationDestinationKey(token, app, deviceId);
		this.active = true;
	}

	@Override
	public Class<NotificationDestinationKey> getKeyClass(){
		return NotificationDestinationKey.class;
	}

	@Override
	public NotificationDestinationKey getKey(){
		return key;
	}

	public Boolean getActive(){
		return active;
	}

	public void setActive(boolean active){
		this.active = active;
	}

	public void setDeviceName(String deviceName){
		this.deviceName = deviceName;
	}

	public String getDeviceName(){
		return deviceName;
	}

	public Date getCreated(){
		return created;
	}

	public void setCreated(Date created){
		this.created = created;
	}

}
