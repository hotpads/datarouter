package com.hotpads.job.record;

import com.hotpads.datarouter.storage.field.enums.DataRouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.StringEnum;

public enum JobExecutionStatus implements StringEnum<JobExecutionStatus>{

	running("running",true),
	success("success",false),
	errored("errored",false),
	interrupted("interrupted",false);
	
	private String varName;
	private boolean isRunning=false;
	
	private JobExecutionStatus(String varName, boolean isRunning){
		this.isRunning = isRunning;
		this.varName = varName;
	}
	
	@Override
	public String getPersistentString(){
		return varName;
	}
	
	public static JobExecutionStatus fromPersistentStringStatic(String s){
		return DataRouterEnumTool.getEnumFromString(values(), s, null);
	}
	
	@Override
	public JobExecutionStatus fromPersistentString(String s) {
		return fromPersistentStringStatic(s);
	}
	
	public boolean isRunning(){
		return isRunning;
	}
}
