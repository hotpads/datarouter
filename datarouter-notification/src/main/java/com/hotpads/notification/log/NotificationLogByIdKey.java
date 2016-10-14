package com.hotpads.notification.log;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.base.BaseEntityPrimaryKey;

public class NotificationLogByIdKey
extends BaseEntityPrimaryKey<NotificationLogByIdEntityKey,NotificationLogByIdKey>{

	private NotificationLogByIdEntityKey entityKey;

	@SuppressWarnings("unused") // used by datarouter reflection
	private NotificationLogByIdKey(){
		this(null);
	}

	public NotificationLogByIdKey(String id){
		this.entityKey = new NotificationLogByIdEntityKey(id);
	}

	@Override
	public NotificationLogByIdEntityKey getEntityKey(){
		return entityKey;
	}

	@Override
	public NotificationLogByIdKey prefixFromEntityKey(NotificationLogByIdEntityKey entityKey){
		return new NotificationLogByIdKey(entityKey.getId());
	}

	@Override
	public List<Field<?>> getPostEntityKeyFields(){
		return Arrays.asList();
	}

}
