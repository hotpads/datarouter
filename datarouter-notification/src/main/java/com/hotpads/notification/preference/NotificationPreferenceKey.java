package com.hotpads.notification.preference;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

public class NotificationPreferenceKey extends BasePrimaryKey<NotificationPreferenceKey>{

	public static class FieldKeys{
		public static final StringFieldKey userToken = new StringFieldKey("userToken");
		public static final StringFieldKey deviceGroup = NotificationDestinationAppGroupName.key.withColumnName(
				"deviceGroup");
		public static final StringFieldKey typeGroup = NotificationTypeGroupName.key.withColumnName("typeGroup");
	}

	private String userToken;
	private NotificationDestinationAppGroupName deviceGroup;
	private NotificationTypeGroupName typeGroup;

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(
				new StringField(FieldKeys.userToken, userToken),
				new StringField("deviceGroup", FieldKeys.deviceGroup, deviceGroup.persistentString),
				new StringField("typeGroup", FieldKeys.typeGroup, typeGroup.persistentString));
	}

	public NotificationPreferenceKey(String userToken, NotificationDestinationAppGroupName deviceGroup,
			NotificationTypeGroupName typeGroup){
		this.userToken = userToken;
		this.deviceGroup = deviceGroup == null ? new NotificationDestinationAppGroupName() : deviceGroup;
		this.typeGroup = typeGroup == null ? new NotificationTypeGroupName() : typeGroup;
	}

	public NotificationPreferenceKey(String userToken){
		this(userToken, new NotificationDestinationAppGroupName(), new NotificationTypeGroupName());
	}

	public NotificationPreferenceKey(){
		this(null);
	}

	public NotificationDestinationAppGroupName getDeviceGroup(){
		return deviceGroup;
	}

	public NotificationTypeGroupName getTypeGroup(){
		return typeGroup;
	}

}
