package com.hotpads.notification.destination;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.BooleanField;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;
import com.hotpads.notification.databean.NotificationUserType;


/** CREATE SCRIPT
com.hotpads.notification.destination.NotificationDestination{
  PK{
    StringEnumField<NotificationUserType> notificationUserType,
    StringField app,
    StringField token
  }
  StringEnumField<NotificationDestinationPlatform> platform,
  StringField deviceId,
  StringField deviceName,
  BooleanField active

}

*/
public class NotificationDestination extends BaseDatabean<NotificationDestinationKey,NotificationDestination> {

	/** fields ****************************************************************/

	private NotificationDestinationKey key;

	private NotificationDestinationPlatform platform;
	private String deviceId;
	private String deviceName;
	private Boolean active;


	/** columns ***************************************************************/

	public static class F {
		public static final String
			platform = "platform",
			deviceId = "deviceId",
			deviceName = "deviceName",
			active = "active";
	}

	/** fielder ***************************************************************/

	public static class NotificationDestinationFielder
		extends BaseDatabeanFielder<NotificationDestinationKey, NotificationDestination>{
		public NotificationDestinationFielder(){
		}

		@Override
		public Class<NotificationDestinationKey> getKeyFielderClass() {
			return NotificationDestinationKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(NotificationDestination d){
			return FieldTool.createList(
				new StringEnumField<>(NotificationDestinationPlatform.class, F.platform, d.platform, MySqlColumnType.MAX_LENGTH_VARCHAR),
				new StringField(F.deviceId, d.deviceId, MySqlColumnType.MAX_LENGTH_VARCHAR),
				new StringField(F.deviceName, d.deviceName, MySqlColumnType.MAX_LENGTH_VARCHAR),
				new BooleanField(F.active, d.active));
		}

	}

	/** construct *************************************************************/

	public NotificationDestination(){
		this.key = new NotificationDestinationKey();

	}

	public NotificationDestination(NotificationUserType notificationUserType, String app, String token){
		this.key = new NotificationDestinationKey(notificationUserType, app, token);
	}

	/** databean **************************************************************/

	@Override
	public Class<NotificationDestinationKey> getKeyClass() {
		return NotificationDestinationKey.class;
	}

	@Override
	public NotificationDestinationKey getKey() {
		return key;
	}

	/** get/set ***************************************************************/

	public void setKey(NotificationDestinationKey key) {
		this.key = key;
	}

	public NotificationDestinationPlatform getPlatform(){
		return platform;
	}

	public void setPlatform(NotificationDestinationPlatform platform){
		this.platform = platform;
	}

	public String getDeviceId(){
		return deviceId;
	}

	public void setDeviceId(String deviceId){
		this.deviceId = deviceId;
	}

	public String getDeviceName(){
		return deviceName;
	}

	public void setDeviceName(String deviceName){
		this.deviceName = deviceName;
	}

	public Boolean getActive(){
		return active;
	}

	public void setActive(Boolean active){
		this.active = active;
	}

	public NotificationUserType getNotificationUserType(){
		return key.getNotificationUserType();
	}

	public void setNotificationUserType(NotificationUserType notificationUserType){
		this.key.setNotificationUserType(notificationUserType);
	}

	public String getApp(){
		return key.getApp();
	}

	public void setApp(String app){
		this.key.setApp(app);
	}

	public String getToken(){
		return key.getToken();
	}

	public void setToken(String token){
		this.key.setToken(token);
	}

}

