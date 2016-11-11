package com.hotpads.handler.exception;

import com.hotpads.datarouter.exception.ExceptionCategory;
import com.hotpads.notification.type.NotificationType;

public class NoOpExceptionNotificationTypeFinder implements ExceptionNotificationTypeFinder{

	@Override
	public Class<? extends NotificationType> getNotificationType(ExceptionCategory exceptionCategory){
		return null;
	}

}
