package com.hotpads.job.record;

import com.hotpads.datarouter.storage.field.enums.DatarouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.StringEnum;

public enum JobExecutionStatus implements StringEnum<JobExecutionStatus>{

	RUNNING("running", true),
	SUCCESS("success", false),
	ERRORED("errored", false),
	INTERRUPTED("interrupted", false);

	private String varName;
	private boolean isRunning = false;

	private JobExecutionStatus(String varName, boolean isRunning){
		this.isRunning = isRunning;
		this.varName = varName;
	}

	@Override
	public String getPersistentString(){
		return varName;
	}

	public static JobExecutionStatus fromPersistentStringStatic(String str){
		return DatarouterEnumTool.getEnumFromString(values(), str, null);
	}

	@Override
	public JobExecutionStatus fromPersistentString(String str) {
		return fromPersistentStringStatic(str);
	}

	public boolean isRunning(){
		return isRunning;
	}
}
