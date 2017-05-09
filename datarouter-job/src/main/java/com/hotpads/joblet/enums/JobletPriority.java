package com.hotpads.joblet.enums;

import com.hotpads.datarouter.util.core.DrStringTool;

public enum JobletPriority{
	HIGH(10),
	DEFAULT(100),
	LOW(1000);

	private Integer executionOrder;
	private String comparableName;

	private JobletPriority(Integer executionOrder){
		this.executionOrder = executionOrder;
		this.comparableName = DrStringTool.pad(Integer.toString(executionOrder), '0', 4);
	}

	public static JobletPriority fromExecutionOrder(Integer executionOrder){
		if(executionOrder == null){
			return JobletPriority.LOW;
		}
		for(JobletPriority priority : JobletPriority.values()){
			if(priority.getExecutionOrder() >= executionOrder){
				return priority;
			}
		}
		return JobletPriority.LOW;
	}

	/**
	 * return true if this has higher priority than the other
	 */
	public boolean isHigher(JobletPriority other){
		return executionOrder < other.executionOrder;
	}

	public Integer getExecutionOrder(){
		return this.executionOrder;
	}

	public String getComparableName(){
		return comparableName;
	}

}
