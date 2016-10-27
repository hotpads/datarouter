package com.hotpads.joblet.enums;

import java.util.Arrays;

import com.hotpads.joblet.queue.JobletRequestSelector;
import com.hotpads.joblet.queue.selector.JdbcLockForUpdateJobletRequestSelector;
import com.hotpads.joblet.queue.selector.JdbcUpdateAndScanJobletRequestSelector;
import com.hotpads.joblet.queue.selector.SqsJobletRequestSelector;

public enum JobletQueueMechanism{
	JDBC_LOCK_FOR_UPDATE("jdbcLockForUpdate", JdbcLockForUpdateJobletRequestSelector.class),
	JDBC_UPDATE_AND_SCAN("jdbcUpdateAndScan", JdbcUpdateAndScanJobletRequestSelector.class),
	SQS("sqs", SqsJobletRequestSelector.class);

	private final String persistentString;
	private final Class<? extends JobletRequestSelector> selectorClass;

	private JobletQueueMechanism(String persistentString, Class<? extends JobletRequestSelector> selectorClass){
		this.persistentString = persistentString;
		this.selectorClass = selectorClass;
	}

	public static JobletQueueMechanism fromPersistentString(String from){
		return Arrays.stream(values())
				.filter(mechanism -> mechanism.getPersistentString().equals(from))
				.findAny()
				.get();
	}

	public String getPersistentString(){
		return persistentString;
	}

	public Class<? extends JobletRequestSelector> getSelectorClass(){
		return selectorClass;
	}
}
