package com.hotpads.config.job.enums;

import com.hotpads.datarouter.storage.field.enums.DatarouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.StringEnum;

public enum JobletStatus implements StringEnum<JobletStatus>{

	created("created",false),
	running("running",true),
	runningFailed("runningFailed",true),
	complete("complete",false),
	failed("failed",false),
	timedOut("timedOut",false);
	
	private String varName;
	private boolean isRunning=false;
	
	private JobletStatus(String varName, boolean isRunning){
		this.isRunning = isRunning;
		this.varName = varName;
	}
	
	@Override
	public String getPersistentString(){
		return varName;
	}
	
	public static JobletStatus fromPersistentStringStatic(String s){
		return DatarouterEnumTool.getEnumFromString(values(), s, null);
	}
	
	@Override
	public JobletStatus fromPersistentString(String s) {
		return fromPersistentStringStatic(s);
	}
	
	public boolean isRunning(){
		return isRunning;
	}
}
