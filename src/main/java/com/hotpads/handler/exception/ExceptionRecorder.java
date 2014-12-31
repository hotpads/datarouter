package com.hotpads.handler.exception;

import com.hotpads.notification.type.NotificationType;

public interface ExceptionRecorder{

	void tryRecordException(Exception exception);

	void tryRecordException(Exception exception, Class<? extends NotificationType> notificationType);

}
