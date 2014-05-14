package com.hotpads.job.record;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Date;

import com.google.inject.BindingAnnotation;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.util.datastructs.MutableBoolean;

public class LongRunningTaskTracker {

	@BindingAnnotation 
	@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD }) 
	@Retention(RetentionPolicy.RUNTIME)
	public @interface LongRunningTaskNode {}
	
	static long HEARTBEAT_PERSIST_PERIOD_MS = 2000L;
	
	private IndexedSortedMapStorageNode node;
	private LongRunningTask task;
	private MutableBoolean interrupted;
	private Date lastPersistedHeartbeat;
	
	public LongRunningTaskTracker(IndexedSortedMapStorageNode node, LongRunningTask task){
		this.node = node;
		this.task = task;
		this.interrupted = new MutableBoolean(false);
	}
	
//	private void requestInterrupt(){
//	}
//	
//	private boolean isInterruptRequested(){
//		return false;
//	}
	
	private void heartbeat(){
		if(shouldPersistHeartbeat()){
			Date heartbeat = new Date();
			task.setHeartbeatTime(heartbeat);
			node.put(task, null);
			lastPersistedHeartbeat = heartbeat;
		}
	}
	
	private boolean shouldPersistHeartbeat(){
		return System.currentTimeMillis() - lastPersistedHeartbeat.getTime() > HEARTBEAT_PERSIST_PERIOD_MS;
	}

	public IndexedSortedMapStorageNode getNode() {
		return node;
	}

	public LongRunningTask getTask() {
		return task;
	}

	public void setTask(LongRunningTask task) {
		this.task = task;
	}

	public MutableBoolean getInterrupted() {
		return interrupted;
	}

	public void setInterrupted(MutableBoolean interrupted) {
		this.interrupted = interrupted;
	}

	public Date getLastPersistedHeartbeat() {
		return lastPersistedHeartbeat;
	}

	public void setLastPersistedHeartbeat(Date lastPersistedHeartbeat) {
		this.lastPersistedHeartbeat = lastPersistedHeartbeat;
	}
}
