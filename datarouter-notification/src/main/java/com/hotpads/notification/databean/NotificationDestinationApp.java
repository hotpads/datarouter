package com.hotpads.notification.databean;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.field.imp.array.DelimitedStringArrayField;
import com.hotpads.datarouter.storage.field.imp.array.DelimitedStringArrayFieldKey;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumFieldKey;
import com.hotpads.notification.destination.NotificationDestinationAppName;
import com.hotpads.notification.destination.NotificationPlatform;
import com.hotpads.notification.preference.NotificationDestinationAppGroupName;
import com.hotpads.util.core.stream.StreamTool;

public class NotificationDestinationApp extends BaseDatabean<NotificationDestinationAppKey,NotificationDestinationApp>{
	private NotificationDestinationAppKey key;
	private NotificationDestinationAppGroupName groupName;
	private NotificationPlatform platform;
	private List<String> acceptedUserTypes;

	public NotificationDestinationApp(){
		this.key = new NotificationDestinationAppKey();
		this.groupName = new NotificationDestinationAppGroupName();
	}

	public NotificationDestinationApp(NotificationDestinationAppName name, NotificationPlatform platform,
			NotificationDestinationAppGroupName groupName, List<NotificationUserType> acceptedUserTypes){
		this.key = new NotificationDestinationAppKey(name);
		this.groupName = groupName == null ? new NotificationDestinationAppGroupName() : groupName;
		this.platform = platform;
		this.acceptedUserTypes = StreamTool.map(acceptedUserTypes, NotificationUserType::getPersistentString);
	}

	@Override
	public Class<NotificationDestinationAppKey> getKeyClass(){
		return NotificationDestinationAppKey.class;
	}

	@Override
	public NotificationDestinationAppKey getKey(){
		return key;
	}

	public NotificationDestinationAppGroupName getGroupName(){
		return groupName;
	}

	public void setGroupName(NotificationDestinationAppGroupName groupName){
		this.groupName = groupName;
	}

	public NotificationPlatform getPlatform(){
		return platform;
	}

	public void setPlatform(NotificationPlatform platform){
		this.platform = platform;
	}

	public List<NotificationUserType> getAcceptedUserTypes(){
		return StreamTool.map(acceptedUserTypes, name -> NotificationUserType.ALIAS.fromPersistentString(name));
	}

	public void setAcceptedUserTypes(List<NotificationUserType> acceptedUserTypes){
		this.acceptedUserTypes = StreamTool.map(acceptedUserTypes, NotificationUserType::getPersistentString);
	}

	public static class FieldKeys{
		public static final StringFieldKey groupName = NotificationDestinationAppGroupName.key.withColumnName(
				"groupName");
		public static final StringEnumFieldKey<NotificationPlatform> platform = new StringEnumFieldKey<>("platform",
				NotificationPlatform.class);
		public static final DelimitedStringArrayFieldKey acceptedUserTypes = new DelimitedStringArrayFieldKey(
				"acceptedUserTypes");
	}

	public static class NotificationDestinationAppFielder extends
			BaseDatabeanFielder<NotificationDestinationAppKey,NotificationDestinationApp>{
		public NotificationDestinationAppFielder(){
			super(NotificationDestinationAppKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(NotificationDestinationApp databean){
			return Arrays.asList(
					new StringField("groupName", FieldKeys.groupName, databean.groupName.persistentString),
					new StringEnumField<>(FieldKeys.platform, databean.platform),
					new DelimitedStringArrayField(FieldKeys.acceptedUserTypes, databean.acceptedUserTypes));
		}
	}
}
