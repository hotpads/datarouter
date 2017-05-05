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

public class NotificationTemplateBean extends BaseDatabean<NotificationTemplateBeanKey,NotificationTemplateBean>{
	private NotificationTemplateBeanKey key;
	private String fullClassName;
	private Boolean shouldUseNewSender;

	public NotificationTemplateBean(){
		this.key = new NotificationTemplateBeanKey();
	}

	public NotificationTemplateBean(String name, String fullClassName){
		this.key = new NotificationTemplateBeanKey(name);
		this.fullClassName = fullClassName;
		this.shouldUseNewSender = false;
	}

	public NotificationTemplateBean(String name, String fullClassName, Boolean shouldUseNewSender){
		this.key = new NotificationTemplateBeanKey(name);
		this.fullClassName = fullClassName;
		this.shouldUseNewSender = shouldUseNewSender;
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

	public Boolean getShouldUseNewSender(){
		return shouldUseNewSender;
	}

	public static class FieldKeys{
		public static final StringFieldKey fullClassName = new StringFieldKey("fullClassName");
		public static final BooleanFieldKey shouldUseNewSender = new BooleanFieldKey("shouldUseNewSender");
	}

	public static class NotificationTemplateBeanFielder
	extends BaseDatabeanFielder<NotificationTemplateBeanKey,NotificationTemplateBean>{
		public NotificationTemplateBeanFielder(){
			super(NotificationTemplateBeanKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(NotificationTemplateBean databean){
			return Arrays.asList(
					new StringField(FieldKeys.fullClassName, databean.fullClassName),
					new BooleanField(FieldKeys.shouldUseNewSender, databean.shouldUseNewSender));
		}
	}
}
