package com.hotpads.handler.exception;

import com.hotpads.notification.type.NotificationType;

public interface ExceptionRecorder{

	ExceptionRecord tryRecordException(Exception exception, String fallbackLocation);

	ExceptionRecord tryRecordException(Exception exception, String fallbackLocation,
			Class<? extends NotificationType> notificationType);

}
