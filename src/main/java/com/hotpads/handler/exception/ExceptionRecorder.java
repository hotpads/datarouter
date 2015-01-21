package com.hotpads.handler.exception;

import com.hotpads.notification.type.NotificationType;

public interface ExceptionRecorder{

	ExceptionRecord tryRecordException(Exception exception);

	ExceptionRecord tryRecordException(Exception exception, Class<? extends NotificationType> notificationType);

}
