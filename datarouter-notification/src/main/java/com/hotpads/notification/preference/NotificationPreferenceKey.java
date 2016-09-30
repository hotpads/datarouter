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
		public static final StringFieldKey deviceGroup = new StringFieldKey(NotificationDeviceGroup.F.persistentString)
				.withColumnName("deviceGroup");
		public static final StringFieldKey typeGroup = new StringFieldKey(NotificationTypeGroup.F.persistentString)
				.withColumnName("typeGroup");
	}

	private String userToken;
	private NotificationDeviceGroup deviceGroup;
	private NotificationTypeGroup typeGroup;

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(
				new StringField(FieldKeys.userToken, userToken),
				new StringField("deviceGroup", FieldKeys.deviceGroup, deviceGroup.persistentString),
				new StringField("typeGroup", FieldKeys.typeGroup, typeGroup.persistentString));
	}

	public NotificationPreferenceKey(String userToken, NotificationDeviceGroup deviceGroup, NotificationTypeGroup
			typeGroup){
		this.userToken = userToken;
		this.deviceGroup = deviceGroup;
		this.typeGroup = typeGroup;
	}

	public NotificationPreferenceKey(String userToken){
		this(userToken, new NotificationDeviceGroup(null), new NotificationTypeGroup(null));
	}

	public NotificationPreferenceKey(){
		this(null);
	}

	public NotificationDeviceGroup getDeviceGroup(){
		return deviceGroup;
	}

	public NotificationTypeGroup getTypeGroup(){
		return typeGroup;
	}

}
