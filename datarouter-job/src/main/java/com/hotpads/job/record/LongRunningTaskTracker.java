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
	private final Setting<Boolean> persistSetting;
	private Date dateLastPersisted;

	public LongRunningTaskTracker(IndexedSortedMapStorage<LongRunningTaskKey,LongRunningTask> node,
			LongRunningTask task, Setting<Boolean> persistSetting){
		this.node = node;
		this.task = task;
		this.interrupted = new MutableBoolean(false);
		this.persistSetting = persistSetting;
	}


	public LongRunningTaskTracker heartbeat(long numItemsProcessed){
		task.setNumItemsProcessed(numItemsProcessed);
		return heartbeat();
	}

	public LongRunningTaskTracker heartbeat(){
		Date now = new Date();
		task.setHeartbeatTime(now);
		persistIfShould(now);
		return this;
	}

	@Deprecated//use hearbeat(numItemsProcessed)
	public LongRunningTaskTracker setNumItemsProcessed(long numItems){
		task.setNumItemsProcessed(numItems);
		return this;
	}

	public void requestStop(){
		logger.info("requested interrupt on " + task.getKey().getJobClass());
		interrupted.set(true);
		task.setInterrupt(true);
	}

	public boolean isStopRequested(){
		if(interrupted.get()){
			task.setJobExecutionStatus(JobExecutionStatus.interrupted);
			persistIfShould(new Date());
		}
		return interrupted.get();
	}

	/*------------------------ private -------------------*/

	private void persistIfShould(Date newDateLastPersisted){
		if(!shouldPersist()){
			return;
		}
		if(task.getKey().getTriggerTime()==null){
			logger.error("not persisting " + task.getDatabeanName() + " tracker because of null trigger time");
		}
		node.put(task, null);
		dateLastPersisted = newDateLastPersisted;
	}

	private boolean shouldPersist(){
		if(persistSetting == null || !persistSetting.getValue()){
			return false;
		}
		if(dateLastPersisted == null){
			return true;
		}
		return System.currentTimeMillis() - dateLastPersisted.getTime() > HEARTBEAT_PERSIST_PERIOD_MS;
	}

	/*--------------------- get/set -------------------------*/

	public IndexedSortedMapStorage<LongRunningTaskKey,LongRunningTask> getNode() {
		return node;
	}

	public LongRunningTask getTask() {
		return task;
	}
}
