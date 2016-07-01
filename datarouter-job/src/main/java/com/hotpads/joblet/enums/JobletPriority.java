package com.hotpads.joblet.enums;

public enum JobletPriority{
	HIGH(10),
	DEFAULT(100),
	MEDIUM_LOW(750),
	LOW(1000),
	LOWER(2000),
	LOWEST(3000);

	private Integer value;

	private JobletPriority(Integer value){
		this.value = value;
	}

	public Integer getValue(){
		return this.value;
	}
}
