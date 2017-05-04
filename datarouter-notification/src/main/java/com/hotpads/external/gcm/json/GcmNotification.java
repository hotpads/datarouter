package com.hotpads.external.gcm.json;

import com.google.gson.annotations.SerializedName;

/**
 * the "notification" component of a GCM message - mainly used by iOS
 */

public class GcmNotification{

	private final String body;
	private final Integer badge;
	@SerializedName("click_action")
	private final String clickAction;

	public GcmNotification(String body, Integer badge, String clickAction){
		this.body = body;
		this.badge = badge;
		this.clickAction = clickAction;
	}

}
