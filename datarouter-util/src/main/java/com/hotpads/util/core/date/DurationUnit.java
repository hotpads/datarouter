package com.hotpads.util.core.date;


public enum DurationUnit{
	YEARS(0, "year"),
	MONTHS(1, "month"),
	DAYS(2, "day"),
	HOURS(3, "hour"),
	MINUTES(4, "minute"),
	SECONDS(5, "second"),
	MILLISECONDS(6, "millisecond");

	private Integer index;
	private String display;

	private DurationUnit(Integer index, String display){
		this.index = index;
		this.display = display;
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
