package com.hotpads.external.gcm.json;

import com.google.gson.JsonObject;

public class GcmRequest{

	private static final String PRIORITY_HIGH = "high";

	public final String to;
	public final boolean contentAvailble = true;
	public final GcmNotification notification;
	public final JsonObject data;
	public final String priority = PRIORITY_HIGH;

	public GcmRequest(String to, GcmNotification notification, JsonObject data){
		this.to = to;
		this.notification = notification;
		this.data = data;
	}

}
