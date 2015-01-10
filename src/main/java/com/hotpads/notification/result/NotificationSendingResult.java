package com.hotpads.notification.result;

import java.util.List;

import com.hotpads.notification.databean.NotificationUserId;
import com.hotpads.notification.sender.template.NotificationTemplate;


public class NotificationSendingResult{

	private NotificationUserId userId;
	private Class<? extends NotificationTemplate> template;
	private FailureReason failureReason;

	public NotificationSendingResult(NotificationUserId userId, Class<? extends NotificationTemplate> template){
		this.userId = userId;
		this.template = template;
	}

	public FailureReason getFailureReason(){
		return failureReason;
	}

	public void setFail(FailureReason failureReason){
		this.failureReason = failureReason;
	}

	public void setFailIfnotSet(FailureReason failureReason){
		if(this.failureReason == null){
			this.failureReason = failureReason;
		}
	}

	public static int getSuccesCount(List<NotificationSendingResult> results){
		int count = 0;
		for(NotificationSendingResult result : results){
			if(result.getFailureReason() == null){
				count++;
			}
		}
		return count;
	}

}
