package com.hotpads.datarouter.storage.view;

import java.util.HashSet;
import java.util.Set;

import com.hotpads.util.core.enums.EnumTool;
import com.hotpads.util.core.enums.HpEnum;

public enum ViewRenderingStatus implements HpEnum{
	UNKNOWN(-1, "Unknown"),
	NEW(9, "New"),
	OK(10, "OK"),
	MARKED(11, "Marked"),
	QUEUED(12, "Queued"),
	MARKED_LOW_PRIORITY(13, "Marked Low Priority"),
	QUEUED_LOW_PRIORITY(14, "Queued Low Priority"),
	FAILED(20, "Failed");
	
	public static ViewRenderingStatus fromPersistentInteger(Integer value){
		if(value==null){ return UNKNOWN; }
		switch(value){
		case 9: return NEW;
		case 10: return OK;
		case 11: return MARKED;
		case 12: return QUEUED;
		case 13: return MARKED_LOW_PRIORITY;
		case 14: return QUEUED_LOW_PRIORITY;
		case 20: return FAILED;
		default: return UNKNOWN;
		}
	}

	private Integer persistentInteger;
	private String display;
	
	private ViewRenderingStatus(Integer persistentInteger, String display){
		this.persistentInteger = persistentInteger;
		this.display = display;
	}
	
	public String getDisplay() { 
		return display; 
	}
	
	public static ViewRenderingStatus fromDisplay(String display) {
		return EnumTool.getEnumFromDisplay(values(), display, UNKNOWN);
	}
	
	public Integer getPersistentInteger(){
		return persistentInteger;
	}
	
	public static Set<ViewRenderingStatus> allMarked = new HashSet<ViewRenderingStatus>();
	static{
		allMarked.add(MARKED);
		allMarked.add(MARKED_LOW_PRIORITY);
	}
	
	public static Set<ViewRenderingStatus> allQueued = new HashSet<ViewRenderingStatus>();
	static{
		allQueued.add(QUEUED);
		allQueued.add(QUEUED_LOW_PRIORITY);
	}
	public Integer getInteger() {
		return persistentInteger;
	}
}
