package com.hotpads.util.core.date;


public enum DurationUnit{
	YEARS(0, "year"),
	MONTHS(1, "month"),
	DAYS(2, "day"),
	HOURS(3, "hour"),
	MINUTES(4, "minute"),
	SECONDS(5, "second"),
	MILLISECONDS(6, "millisecond");
	
	Integer index;
	String display;
	
	private DurationUnit(Integer index, String display){
		this.index = index;
		this.display = display;
	}
	
	public static DurationUnit fromIndex(int index){
		for (DurationUnit du : values()){
			if (index == du.index) return du;
		}
		return null;
	}
	
	public Integer getIndex(){
		return index;
	}
	
	public String getDisplay(){
		return display;
	}
	
	public String getDisplayPlural(){
		return display + "s";
	}
}
