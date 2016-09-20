package com.hotpads.notification.databean;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.enums.StringEnumField;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.entity.base.BaseEntityKey;

@SuppressWarnings("serial")
public abstract class BaseNotificationUserIdEntityKey<EK extends EntityKey<EK>> extends BaseEntityKey<EK>{

	public static final String PREFIX_userId = "userId";

	private NotificationUserId userId;

	public BaseNotificationUserIdEntityKey(NotificationUserId userId){
		this.userId = userId;
	}

	@Override
	public List<Field<?>> getFields() {
		return Arrays.asList(
				new StringEnumField<>(NotificationUserId.FieldKeys.userType, userId.getType(), PREFIX_userId),
				new StringField(PREFIX_userId, NotificationUserId.FieldKeys.userId, userId.getId()));
	}

	public NotificationUserId getUserId(){
		return userId;
	}

}