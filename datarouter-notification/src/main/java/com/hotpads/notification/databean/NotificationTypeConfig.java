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

public class NotificationTypeConfig extends BaseDatabean<NotificationTypeConfigKey,NotificationTypeConfig>{
	private NotificationTypeConfigKey key;

	private String name;
	private String group;
	private Boolean needsRemoveDisabledCallback;
	private Boolean needsFilterOutIrrelevantCallback;
	private Boolean needsOnSuccessCallback;

	public NotificationTypeConfig(){
		this.key = new NotificationTypeConfigKey();
	}

	public NotificationTypeConfig(String type, String name, String group, Boolean needsRemoveDisabledCallback,
			Boolean needsFilterOutIrrelevantCallback, Boolean needsOnSuccessCallback){
		this.key = new NotificationTypeConfigKey(type);
		this.name = name;
		this.group = group;
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

	public String getGroup(){
		return group;
	}

	public void setGroup(String group){
		this.group = group;
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
		public static final StringFieldKey group = new StringFieldKey("group");
		public static final BooleanFieldKey needsRemoveDisabledCallback = new BooleanFieldKey(
				"needsRemoveDisabledCallback");
		public static final BooleanFieldKey needsFilterOutIrrelevantCallback = new BooleanFieldKey(
				"needsFilterOutIrrelevantCallback");
		public static final BooleanFieldKey needsOnSuccessCallback = new BooleanFieldKey("needsOnSuccessCallback");
	}

	public static class NotificationTypeBeanFielder
	extends BaseDatabeanFielder<NotificationTypeConfigKey,NotificationTypeConfig>{
		public NotificationTypeBeanFielder(){
			super(NotificationTypeConfigKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(NotificationTypeConfig databean){
			return Arrays.asList(
					new StringField(FieldKeys.name, databean.name),
					new StringField(FieldKeys.group, databean.group),
					new BooleanField(FieldKeys.needsRemoveDisabledCallback, databean.needsRemoveDisabledCallback),
					new BooleanField(FieldKeys.needsFilterOutIrrelevantCallback, databean
							.needsFilterOutIrrelevantCallback),
					new BooleanField(FieldKeys.needsOnSuccessCallback, databean.needsOnSuccessCallback));
		}
	}
}
