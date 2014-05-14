package com.hotpads.job.record;

import java.util.Date;

import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.util.datastructs.MutableBoolean;

public class LongRunningTaskTracker {

	static long HEARTBEAT_PERSIST_PERIOD_MS = 2000L;
	
	private IndexedSortedMapStorageNode node;
	private LongRunningTask task;
	private MutableBoolean interrupted;
	Date lastPersistedHeartbeat;
	
	public LongRunningTaskTracker(IndexedSortedMapStorageNode node, LongRunningTask task){
		this.node = node;
		this.task = task;
	}
	
//	private void requestInterrupt(){
//	}
//	
//	private boolean isInterruptRequested(){
//		return false;
//	}
	
	private void heartbeat(){
		if(shouldPersistHeartbeat()){
			task.setHeartbeatTime(new Date(System.currentTimeMillis()));
			node.put(task, null);
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
