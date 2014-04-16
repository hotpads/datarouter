package com.hotpads.profile.count.databean.alert;

import com.hotpads.datarouter.storage.field.enums.StringEnum;
import com.hotpads.util.core.enums.EnumTool;
import com.hotpads.util.core.enums.StringPersistedEnum;


public enum CounterAlertNotificationType implements StringEnum<CounterAlertNotificationType>,StringPersistedEnum{
	PHONE("phone","Telephone Number"),
	EMAIL("email","Email Address"),
	;
	private String notificationType, display;

	private CounterAlertNotificationType(String notificationType, String display){
		this.notificationType = notificationType;
		this.display = display;
	}
	
	@Override
	public String getDisplay() { return display; }

	@Override
	public String getPersistentString() { return notificationType;}
	
	public CounterAlertNotificationType fromPersistentString(String s){
		return fromPersistentStringStatic(s);
	}
	public static CounterAlertNotificationType fromPersistentStringStatic(String s){
		return EnumTool.fromPersistentString(values(), s);
	}
}
