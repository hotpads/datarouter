package com.hotpads.handler.exception;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.exception.ExceptionCategory;
import com.hotpads.handler.exception.WebExceptionCategory;
import com.hotpads.job.trigger.JobExceptionCategory;
import com.hotpads.notification.type.NotificationType;

@Singleton
public class ExceptionCategoryNotificationTypeFactory{

	@Inject
	private ExceptionHandlingConfig exceptionHandlingConfig;


	public Class<? extends NotificationType> getNotificationType(ExceptionCategory exceptionCategory){
		if(WebExceptionCategory.HTTP_REQUEST == exceptionCategory){
			return exceptionHandlingConfig.getServerErrorNotificationType();
		}else if(JobExceptionCategory.JOB == exceptionCategory){
			return exceptionHandlingConfig.getJobErrorNotificationType();
		}else if(JobExceptionCategory.JOBLET == exceptionCategory){
			return exceptionHandlingConfig.getJobletErrorNotificationType();
		}else{
			return exceptionHandlingConfig.getDefaultErrorNotificationType();
		}
	}
}
