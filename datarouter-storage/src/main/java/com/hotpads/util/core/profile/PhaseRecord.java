package com.hotpads.util.core.profile;

public class PhaseRecord {
	String name;
	String threadId;
	long time;
	long duration = 0L;
	
	public static final String makeThreadId( long threadId ) {
		return "T" + threadId;
	}
	public PhaseRecord() {
		this.threadId = makeThreadId(Thread.currentThread().getId());
		this.name = threadId;
		this.time = System.currentTimeMillis();
	}
	public PhaseRecord( String name ) {
		this.name = name == null ? "" : name;
		this.threadId = makeThreadId(Thread.currentThread().getId());
		this.time = System.currentTimeMillis();
	}

	/*** Standard getters/setters ***/
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getThreadId() {
		return threadId;
	}
	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}

	/***
	 * Overrides
	 */
	@Override
	public String toString() {
		return threadId + ( name.length() == 0 ? "" : (":" + name) + " @" + time);
	}

}
