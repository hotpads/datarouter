package com.hotpads.job.record;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage;
import com.hotpads.datarouter.setting.Setting;
import com.hotpads.util.datastructs.MutableBoolean;

public class LongRunningTaskTracker {
	private static Logger logger = LoggerFactory.getLogger(LongRunningTaskTracker.class);

	private static long HEARTBEAT_PERSIST_PERIOD_MS = 2000L;

	private final IndexedSortedMapStorage<LongRunningTaskKey, LongRunningTask> node;
	private final LongRunningTask task;
	private final MutableBoolean interrupted;
	private final Setting<Boolean> shouldPersist;
	private Date lastPersistedHeartbeat;

	public LongRunningTaskTracker(IndexedSortedMapStorage<LongRunningTaskKey,LongRunningTask> node,
			LongRunningTask task, Setting<Boolean> shouldPersist){
		this.node = node;
		this.task = task;
		this.interrupted = new MutableBoolean(false);
		this.shouldPersist = shouldPersist;
	}


	public LongRunningTaskTracker heartbeat(){
		if( ! shouldPersistHeartbeat()){
			Date heartbeat = new Date();
			task.setHeartbeatTime(heartbeat);
			persist();
			lastPersistedHeartbeat = heartbeat;
		}
		return this;
	}

	public LongRunningTaskTracker setNumItemsProcessed(long numItems){
		task.setNumItemsProcessed(numItems);
		return this;
	}

	public void requestStop(){
		logger.info("requested interrupt on "+task.getKey().getJobClass());
		interrupted.set(true);
		task.setInterrupt(true);
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

	/*------------------------ private -------------------*/

	private void persist(){
		if(task.getKey().getTriggerTime()==null){
			logger.error("not persisting "+task.getDatabeanName()+" tracker because of null trigger time");
		}
		node.put(task, null);
	}

	private boolean shouldPersistHeartbeat(){
		if((shouldPersist == null) || !shouldPersist.getValue()){
			return false;
		}
		if(lastPersistedHeartbeat == null){
			return true;
		}
		return System.currentTimeMillis() - lastPersistedHeartbeat.getTime() > HEARTBEAT_PERSIST_PERIOD_MS;
	}

	/*--------------------- get/set -------------------------*/

	public IndexedSortedMapStorage<LongRunningTaskKey,LongRunningTask> getNode() {
		return node;
	}

	public LongRunningTask getTask() {
		return task;
	}
}
