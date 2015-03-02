package com.hotpads.job.record;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.setting.Setting;
import com.hotpads.util.datastructs.MutableBoolean;

public class LongRunningTaskTracker {

	private static Logger logger = LoggerFactory.getLogger(LongRunningTaskTracker.class);

	static long HEARTBEAT_PERSIST_PERIOD_MS = 2000L;
	
	private IndexedSortedMapStorage<LongRunningTaskKey, LongRunningTask> node;
	private LongRunningTask task;
	private MutableBoolean interrupted;
	private Date lastPersistedHeartbeat;
	private Setting<Boolean> shouldSaveLongRunningTasks;
	
	public LongRunningTaskTracker(IndexedSortedMapStorage<LongRunningTaskKey, LongRunningTask> node,
			LongRunningTask task, Setting<Boolean> shouldSaveLongRunningTasks){
		this.node = node;
		this.task = task;
		this.interrupted = new MutableBoolean(false);
		this.shouldSaveLongRunningTasks = shouldSaveLongRunningTasks;
	}
	
	public void requestStop(){
		interrupted.set(true);
	}
	
	public boolean isStopRequested(){
		if(interrupted.get()){
			task.setJobExecutionStatus(JobExecutionStatus.interrupted);
			if(shouldPersistHeartbeat()){
				persist();
			}
		}
		return interrupted.get();
	}
	
	public LongRunningTaskTracker heartbeat(){
		if(shouldPersistHeartbeat()){
			Date heartbeat = new Date();
			task.setHeartbeatTime(heartbeat);
			persist();
			lastPersistedHeartbeat = heartbeat;
		}
		return this;
	}
	
	private void persist(){
		if(task.getKey().getTriggerTime()==null){
			logger.error("not persisting "+task.getDatabeanName()+" tracker because of null trigger time");
		}
		node.put(task, null);
	}
	
	private boolean shouldPersistHeartbeat(){
		if((shouldSaveLongRunningTasks == null) || !shouldSaveLongRunningTasks.getValue()){
			return false;
		}
		if(lastPersistedHeartbeat == null){
			return true;
		}
		return System.currentTimeMillis() - lastPersistedHeartbeat.getTime() > HEARTBEAT_PERSIST_PERIOD_MS;
	}
	
	public LongRunningTaskTracker setNumItemsProcessed(int numItems){
		task.setNumItemsProcessed(numItems);
		return this;
	}
	
	public IndexedSortedMapStorage<LongRunningTaskKey, LongRunningTask> getNode() {
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

	public Setting<Boolean> getShouldSaveLongRunningTasks() {
		return shouldSaveLongRunningTasks;
	}

	public void setShouldSaveLongRunningTasks(Setting<Boolean> shouldSaveLongRunningTasks) {
		this.shouldSaveLongRunningTasks = shouldSaveLongRunningTasks;
	}
}
