package com.hotpads.notification.databean;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.field.imp.comparable.BooleanField;
import com.hotpads.datarouter.storage.field.imp.comparable.BooleanFieldKey;
import com.hotpads.notification.preference.NotificationTypeGroupName;

public class NotificationTypeConfig extends BaseDatabean<NotificationTypeConfigKey,NotificationTypeConfig>{
	private NotificationTypeConfigKey key;

	private String name;
	private NotificationTypeGroupName groupName;
	private Boolean needsRemoveDisabledCallback;
	private Boolean needsFilterOutIrrelevantCallback;
	private Boolean needsOnSuccessCallback;

	public NotificationTypeConfig(){
		this.key = new NotificationTypeConfigKey();
		this.groupName = new NotificationTypeGroupName();
	}

	public NotificationTypeConfig(String type, String name, NotificationTypeGroupName groupName,
			Boolean needsRemoveDisabledCallback, Boolean needsFilterOutIrrelevantCallback,
			Boolean needsOnSuccessCallback){
		this.key = new NotificationTypeConfigKey(type);
		this.name = name;
		this.groupName = groupName == null ? new NotificationTypeGroupName() : groupName;
		this.needsRemoveDisabledCallback = needsRemoveDisabledCallback;
		this.needsFilterOutIrrelevantCallback = needsFilterOutIrrelevantCallback;
		this.needsOnSuccessCallback = needsOnSuccessCallback;
	}

	@Override
	public Class<NotificationTypeConfigKey> getKeyClass(){
		return NotificationTypeConfigKey.class;
	}

	@Override
	public NotificationTypeConfigKey getKey(){
		return key;
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public NotificationTypeGroupName getGroupName(){
		return groupName;
	}

	public void setGroup(NotificationTypeGroupName groupName){
		this.groupName = groupName;
	}

	public Boolean getNeedsRemoveDisabledCallback(){
		return needsRemoveDisabledCallback;
	}

	public void setNeedsRemoveDisabledCallback(Boolean needsRemoveDisabledCallback){
		this.needsRemoveDisabledCallback = needsRemoveDisabledCallback;
	}

	public Boolean getNeedsFilterOutIrrelevantCallback(){
		return needsFilterOutIrrelevantCallback;
	}

	public void setNeedsFilterOutIrrelevantCallback(Boolean needsFilterOutIrrelevantCallback){
		this.needsFilterOutIrrelevantCallback = needsFilterOutIrrelevantCallback;
	}

	public Boolean getNeedsOnSuccessCallback(){
		return needsOnSuccessCallback;
	}

	public void setNeedsOnSuccessCallback(Boolean needsOnSuccessCallback){
		this.needsOnSuccessCallback = needsOnSuccessCallback;
	}

	public static class FieldKeys{
		public static final StringFieldKey name = new StringFieldKey("name");
		public static final StringFieldKey groupName = NotificationTypeGroupName.key.withColumnName("groupName");
		public static final BooleanFieldKey needsRemoveDisabledCallback = new BooleanFieldKey(
				"needsRemoveDisabledCallback");
		public static final BooleanFieldKey needsFilterOutIrrelevantCallback = new BooleanFieldKey(
				"needsFilterOutIrrelevantCallback");
		public static final BooleanFieldKey needsOnSuccessCallback = new BooleanFieldKey("needsOnSuccessCallback");
	}

	public static class NotificationTypeConfigFielder
	extends BaseDatabeanFielder<NotificationTypeConfigKey,NotificationTypeConfig>{
		public NotificationTypeConfigFielder(){
			super(NotificationTypeConfigKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(NotificationTypeConfig databean){
			return Arrays.asList(
					new StringField(FieldKeys.name, databean.name),
					new StringField("groupName", FieldKeys.groupName, databean.groupName.persistentString),
					new BooleanField(FieldKeys.needsRemoveDisabledCallback, databean.needsRemoveDisabledCallback),
					new BooleanField(FieldKeys.needsFilterOutIrrelevantCallback, databean
							.needsFilterOutIrrelevantCallback),
					new BooleanField(FieldKeys.needsOnSuccessCallback, databean.needsOnSuccessCallback));
		}
	}
}
