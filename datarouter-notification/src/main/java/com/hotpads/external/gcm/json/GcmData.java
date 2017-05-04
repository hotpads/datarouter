package com.hotpads.external.gcm.json;

/**
 * parent class for the data payloads sent in GCM messages, which will be GSON'd.
 */
public abstract class GcmData{

	private String type;
	private String notificationId;

	public void setType(String type){
		this.type = type;
	}

	public void setNotificationId(String notificationId){
		this.notificationId = notificationId;
	}

}
