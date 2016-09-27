package com.hotpads.notification.preference;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;

@SuppressWarnings("serial")
public class NotificationPreferenceKey extends BasePrimaryKey<NotificationPreferenceKey>{

	public static class FieldKeys{
		public static final StringFieldKey userToken = new StringFieldKey("userToken");
		public static final StringFieldKey deviceGroup = new StringFieldKey("deviceGroup", false,
				StringField.DEFAULT_STRING_LENGTH);
		public static final StringFieldKey typeGroup = new StringFieldKey("typeGroup", false,
				StringField.DEFAULT_STRING_LENGTH);
	}

	private String userToken;
	private NotificationDeviceGroup deviceGroup;
	private NotificationTypeGroup typeGroup;

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(
				new StringField(FieldKeys.userToken, userToken),
				new StringField(FieldKeys.deviceGroup, deviceGroup.persistentString),
				new StringField(FieldKeys.typeGroup, typeGroup.persistentString));
	}

	public NotificationPreferenceKey(String userToken, NotificationDeviceGroup deviceGroup, NotificationTypeGroup
			typeGroup){
		this.userToken = userToken;
		this.deviceGroup = deviceGroup;
		this.typeGroup = typeGroup;
	}

	public NotificationPreferenceKey(){
		this(null, new NotificationDeviceGroup(null), new NotificationTypeGroup(null));
	}

	public NotificationDeviceGroup getDeviceGroup(){
		return deviceGroup;
	}

	public NotificationTypeGroup getTypeGroup(){
		return typeGroup;
	}

}
