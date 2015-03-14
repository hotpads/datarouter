package com.hotpads.notification.destination;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.BooleanField;
import com.hotpads.datarouter.util.core.DrBooleanTool;

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

public class NotificationDestination extends BaseDatabean<NotificationDestinationKey,NotificationDestination> {

	private NotificationDestinationKey key;

	private String deviceName;
	private Boolean active;

	public static class F {
		public static final String
			deviceName = "deviceName",
			active = "active";
	}

	public static class NotificationDestinationFielder
		extends BaseDatabeanFielder<NotificationDestinationKey, NotificationDestination>{

		@Override
		public Class<NotificationDestinationKey> getKeyFielderClass() {
			return NotificationDestinationKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(NotificationDestination d){
			return FieldTool.createList(
				new StringField(F.deviceName, d.deviceName, MySqlColumnType.MAX_LENGTH_VARCHAR),
				new BooleanField(F.active, d.active));
		}

	}

	private NotificationDestination(){
		this.key = new NotificationDestinationKey();
	}

	public NotificationDestination(String token, NotificationDestinationAppEnum app, String deviceId){
		this.key = new NotificationDestinationKey(token, app, deviceId);
		this.active = true;
	}

	@Override
	public Class<NotificationDestinationKey> getKeyClass() {
		return NotificationDestinationKey.class;
	}

	@Override
	public NotificationDestinationKey getKey() {
		return key;
	}

	public Boolean getActive(){
		return active;
	}

	public void setActive(boolean active){
		this.active = active;
	}

	public static List<NotificationDestination> filterForAppAndActive(Iterable<NotificationDestination> destinations, Collection<NotificationDestinationApp> apps){
		ArrayList<NotificationDestination> activeDestinations = new ArrayList<>();
		for(NotificationDestination destination : destinations){
			if(DrBooleanTool.isTrue(destination.getActive()) && apps.contains(destination.getKey().getApp())){
				activeDestinations.add(destination);
			}
		}
		return activeDestinations;
	}

}

