package com.hotpads.joblet.enums;

import com.hotpads.datarouter.storage.field.enums.DatarouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.StringEnum;

public enum JobletStatus implements StringEnum<JobletStatus>{

	created("created", false),
	running("running", true),
	runningFailed("runningFailed", true),
	complete("complete", false),
	interrupted("interrupted", false),
	failed("failed", false),
	timedOut("timedOut", false);

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

	public static JobletStatus fromPersistentStringStatic(String string){
		return DatarouterEnumTool.getEnumFromString(values(), string, null);
	}

	@Override
	public JobletStatus fromPersistentString(String string){
		return fromPersistentStringStatic(string);
	}

	public boolean isRunning(){
		return isRunning;
	}
}
