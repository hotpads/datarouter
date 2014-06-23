package com.hotpads.job.record;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Date;

import org.apache.log4j.Logger;

import com.google.inject.BindingAnnotation;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.setting.Setting;
import com.hotpads.util.datastructs.MutableBoolean;

public class LongRunningTaskTracker {

	private static Logger logger = Logger.getLogger(LongRunningTaskTracker.class);
	
	@BindingAnnotation 
	@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD }) 
	@Retention(RetentionPolicy.RUNTIME)
	public @interface LongRunningTaskNode {}
	
	static long HEARTBEAT_PERSIST_PERIOD_MS = 2000L;
	
	private IndexedSortedMapStorageNode node;
	private LongRunningTask task;
	private MutableBoolean interrupted;
	private Date lastPersistedHeartbeat;
	private Setting<Boolean> shouldSaveLongRunningTasks;
	
	public LongRunningTaskTracker(IndexedSortedMapStorageNode node, LongRunningTask task, Setting<Boolean> shouldSaveLongRunningTasks){
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
			node.put(task, null);
		}
		return interrupted.get();
	}
	
	public LongRunningTaskTracker heartbeat(){
		if(shouldPersistHeartbeat()){
			Date heartbeat = new Date();
			task.setHeartbeatTime(heartbeat);
			node.put(task, null);
			lastPersistedHeartbeat = heartbeat;
		}
		return this;
	}
	
	private boolean shouldPersistHeartbeat(){
		if(!shouldSaveLongRunningTasks.getValue()){
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

	public Setting<Boolean> getShouldSaveLongRunningTasks() {
		return shouldSaveLongRunningTasks;
	}

	public void setShouldSaveLongRunningTasks(Setting<Boolean> shouldSaveLongRunningTasks) {
		this.shouldSaveLongRunningTasks = shouldSaveLongRunningTasks;
	}
}
