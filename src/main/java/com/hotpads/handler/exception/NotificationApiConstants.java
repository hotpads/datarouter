package com.hotpads.handler.exception;

public class NotificationApiConstants {
	public static final String
		NOTIFICATION_API_ENDPOINT = "http://localhost:8080/job/api/notification";//TODO valide only for dev ?
	
	public static final String 
		NOTIFICATION_API_PARAM_NAME = "requests",
		NOTIFICATION_API_USER_TYPE = "usertype",
		NOTIFICATION_API_USER_ID = "userid",
		NOTIFICATION_API_TIME = "time",
		NOTIFICATION_API_TYPE = "type",
		NOTIFICATION_API_DATA = "data";
	
	public static final String
		SERVER_EXCEPTION_NOTIFICATION_TYPE = "com.hotpads.notification.type.ServerExceptionNotification";
	
	public static final String
		TOKEN_NOTIFICATION_RECIPENT_TYPE = "TOKEN",
		EMAIL_NOTIFICATION_RECIPENT_TYPE = "EMAIL",
		PHONE_NOTIFICATION_RECIPENT_TYPE = "PHONE";
		
}
