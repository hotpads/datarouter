package com.hotpads.datarouter.storage.status;

import com.hotpads.datarouter.storage.field.enums.IntegerEnum;
import com.hotpads.util.core.enums.EnumTool;
import com.hotpads.util.core.enums.HpEnum;

public enum StandardStatus implements HpEnum, IntegerEnum<StandardStatus> {

	UNKNOWN(-1, "Unknown"),
	NEW(9, "New"),
	OK(10, "OK"),
	MARKED(11, "Marked"),
	QUEUED(12, "Queued"),
	INELIGIBLE(13, "Ineligible"),
	MARKED_LOW_PRIORITY(14, "Marked Low Priority"),
	QUEUED_LOW_PRIORITY(15, "Queued Low Priority"),
	FAILED(20, "Failed");
	
	@Override
	public StandardStatus fromPersistentInteger(Integer value){
		return EnumTool.getEnumFromInteger(values(), value, UNKNOWN);
	}

	public static StandardStatus fromInteger(Integer value){
		return EnumTool.getEnumFromInteger(values(), value, UNKNOWN);
	}
	
	private Integer persistentInteger;
	private String display;
	
	private StandardStatus(Integer persistentInteger, String display){
		this.persistentInteger = persistentInteger;
		this.display = display;
	}
	
	public Integer getPersistentInteger(){
		return persistentInteger;
	}

	@Override
	public String getDisplay() { return display; }

	@Override
	public Integer getInteger() { return persistentInteger; }
	
	public boolean isStable(){
		switch(this){
		case OK:
		case FAILED: 
		case INELIGIBLE: return true;
		default:
			return false;
		}
	}
	
	public static StandardStatus fromDisplay(String display) {
		return EnumTool.getEnumFromDisplay(values(), display, UNKNOWN);
	}
}
