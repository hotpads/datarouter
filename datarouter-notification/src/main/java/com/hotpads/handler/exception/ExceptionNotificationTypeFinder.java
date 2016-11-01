package com.hotpads.handler.exception;

import com.hotpads.datarouter.exception.ExceptionCategory;
import com.hotpads.notification.type.NotificationType;

public interface ExceptionNotificationTypeFinder{
	Class<? extends NotificationType> getNotificationType(ExceptionCategory exceptionCategory);
}