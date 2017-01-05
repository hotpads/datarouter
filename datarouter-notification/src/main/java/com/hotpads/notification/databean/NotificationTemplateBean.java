package com.hotpads.notification.databean;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;

public class NotificationTemplateBean extends BaseDatabean<NotificationTemplateBeanKey,NotificationTemplateBean>{
	private NotificationTemplateBeanKey key;
	private String fullClassName;

	public NotificationTemplateBean(){
		this.key = new NotificationTemplateBeanKey();
	}

	public NotificationTemplateBean(String name, String fullClassName){
		this.key = new NotificationTemplateBeanKey(name);
		this.fullClassName = fullClassName;
	}

	@Override
	public Class<NotificationTemplateBeanKey> getKeyClass(){
		return NotificationTemplateBeanKey.class;
	}

	@Override
	public NotificationTemplateBeanKey getKey(){
		return key;
	}

	public String getFullClassName(){
		return fullClassName;
	}

	public void setFullClassName(String fullClassName){
		this.fullClassName = fullClassName;
	}

	public static class FieldKeys{
		public static final StringFieldKey fullClassName = new StringFieldKey("fullClassName");
	}

	public static class NotificationTemplateBeanFielder
	extends BaseDatabeanFielder<NotificationTemplateBeanKey,NotificationTemplateBean>{
		public NotificationTemplateBeanFielder(){
			super(NotificationTemplateBeanKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(NotificationTemplateBean databean){
			return Arrays.asList(new StringField(FieldKeys.fullClassName, databean.fullClassName));
		}
	}
}
