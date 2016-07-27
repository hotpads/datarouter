package com.hotpads.joblet.enums;

import java.util.Arrays;

public enum JobletQueueMechanism{
	JDBC_LOCK_FOR_UPDATE("jdbcLockForUpdate"),
	JDBC_UPDATE_AND_SCAN("jdbcUpdateAndScan");

	private final String persistentString;

	private JobletQueueMechanism(String persistentString){
		this.persistentString = persistentString;
	}

	public static JobletQueueMechanism fromPersistentString(String from){
		return Arrays.stream(values()).filter(mechanism -> mechanism.equals(from)).findAny().get();
	}

	public String getPersistentString(){
		return persistentString;
	}
}
