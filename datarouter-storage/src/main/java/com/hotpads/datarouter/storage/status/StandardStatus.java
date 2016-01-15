package com.hotpads.datarouter.storage.status;

import java.util.EnumSet;

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
	public static final EnumSet<StandardStatus> NEW_OR_MARKED = EnumSet.of(NEW, MARKED);
	public static final EnumSet<StandardStatus> STABLE_STATUSES = EnumSet.of(OK, FAILED, INELIGIBLE);

	private StandardStatus(Integer persistentInteger, String display){
		this.persistentInteger = persistentInteger;
		this.display = display;
	}

	@Override
	public Integer getPersistentInteger(){
		return persistentInteger;
	}

	@Override
	public String getDisplay() {
		return display;
	}

	@Override
	public Integer getInteger() {
		return persistentInteger;
	}

	public boolean isStable(){
		return STABLE_STATUSES.contains(this);
	}

	public static StandardStatus fromDisplay(String display) {
		return EnumTool.getEnumFromDisplay(values(), display, UNKNOWN);
	}
}
