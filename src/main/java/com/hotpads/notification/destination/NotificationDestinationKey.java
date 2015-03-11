package com.hotpads.notification.destination;

import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.notification.databean.NotificationUserType;

@SuppressWarnings("serial")
public class NotificationDestinationKey extends BasePrimaryKey<NotificationDestinationKey> {

	/** fields ****************************************************************/

	private NotificationUserType notificationUserType;
	private String app;
	private String token;

	/** columns ***************************************************************/

	public static class F {
		public static final String
			notificationUserType = "notificationUserType",
			app = "app",
			token = "token";
	}

	@Override
	public List<Field<?>> getFields(){
		return FieldTool.createList(
			new StringEnumField<>(NotificationUserType.class, F.notificationUserType, notificationUserType, MySqlColumnType.MAX_LENGTH_VARCHAR),
			new StringField(F.app, app, MySqlColumnType.MAX_LENGTH_VARCHAR),
			new StringField(F.token, token, MySqlColumnType.MAX_LENGTH_VARCHAR));
	}

	/** construct *************************************************************/

	NotificationDestinationKey(){
	}

	public NotificationDestinationKey(NotificationUserType notificationUserType, String app, String token){
		this.notificationUserType = notificationUserType;
		this.app = app;
		this.token = token;
	}

	/** get/set ***************************************************************/

	public NotificationUserType getNotificationUserType(){
		return notificationUserType;
	}

	public void setNotificationUserType(NotificationUserType notificationUserType){
		this.notificationUserType = notificationUserType;
	}

	public String getApp(){
		return app;
	}

	public void setApp(String app){
		this.app = app;
	}

	public String getToken(){
		return token;
	}

	public void setToken(String token){
		this.token = token;
	}

}
