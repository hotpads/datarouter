package com.hotpads.notification.preference;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.DateFieldKey;

public class NotificationPreference extends BaseDatabean<NotificationPreferenceKey,NotificationPreference>{

	private NotificationPreferenceKey key;
	private Date created;

	public static class FieldKeys{
		public static final DateFieldKey created = new DateFieldKey("created");
	}

	public static class NotificationPreferenceFielder
	extends BaseDatabeanFielder<NotificationPreferenceKey,NotificationPreference>{

		public NotificationPreferenceFielder(){
			super(NotificationPreferenceKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(NotificationPreference databean){
			return Arrays.asList(new DateField(FieldKeys.created, databean.created));
		}

	}

	public NotificationPreference(){
		this.key = new NotificationPreferenceKey();
	}

	public NotificationPreference(String userToken, NotificationDestinationAppGroupName deviceGroup,
			NotificationTypeGroupName typeGroup){
		this.key = new NotificationPreferenceKey(userToken, deviceGroup, typeGroup);
		this.created = new Date();
	}

	@Override
	public Class<NotificationPreferenceKey> getKeyClass(){
		return NotificationPreferenceKey.class;
	}

	@Override
	public NotificationPreferenceKey getKey(){
		return key;
	}

}
