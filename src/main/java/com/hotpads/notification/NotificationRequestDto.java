package com.hotpads.notification;

public class NotificationRequestDto {

	private String userType;
	private String userId;
	private String type;
	private String data;
	private String channel;

	public NotificationRequestDto(String userType, String userId, String type, String data, String channel) {
		this.userType = userType;
		this.userId = userId;
		this.type = type;
		this.data = data;
		this.channel = channel;
	}

	public String getUserType() {
		return userType;
	}

	public String getUserId() {
		return userId;
	}

	public String getType() {
		return type;
	}

	public String getData() {
		return data;
	}

	public String getChannel() {
		return channel;
	}

}
