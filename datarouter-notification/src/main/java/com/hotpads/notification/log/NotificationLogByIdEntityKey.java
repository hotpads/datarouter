package com.hotpads.notification.log;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.entity.base.BaseEntityKey;
import com.hotpads.notification.databean.NotificationLog;

public class NotificationLogByIdEntityKey extends BaseEntityKey<NotificationLogByIdEntityKey>{

	private String id;

	@SuppressWarnings("unused") // used by datarouter reflection
	private NotificationLogByIdEntityKey(){
		this(null);
	}

	public NotificationLogByIdEntityKey(String id){
		this.id = id;
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(new StringField(NotificationLog.FieldKeys.id, id));
	}

	public String getId(){
		return id;
	}

}
