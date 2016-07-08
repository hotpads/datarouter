package com.hotpads.joblet.enums;

public enum JobletPriority{
	HIGH(10),
	DEFAULT(100),
	LOW(1000);

	private Integer executionOrder;

	private JobletPriority(Integer executionOrder){
		this.executionOrder = executionOrder;
	}

	public static JobletPriority fromExecutionOrder(Integer executionOrder){
		if(executionOrder==null){
			return JobletPriority.LOW;
		}
		for(JobletPriority priority : JobletPriority.values()){
			if(priority.getExecutionOrder() >= executionOrder){
				return priority;
			}
		}
		return JobletPriority.LOW;
	}

	public Integer getExecutionOrder(){
		return this.executionOrder;
	}
}
