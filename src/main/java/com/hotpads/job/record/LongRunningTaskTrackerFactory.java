package com.hotpads.job.record;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.job.record.LongRunningTaskTracker.LongRunningTaskNode;
import com.hotpads.setting.Setting;


@Singleton
public class LongRunningTaskTrackerFactory {

	private IndexedSortedMapStorageNode longRunningTaskNode;
	
	@Inject
	public LongRunningTaskTrackerFactory(@LongRunningTaskNode IndexedSortedMapStorageNode longRunningTaskNode){
		this.longRunningTaskNode = longRunningTaskNode;
	}
	
	public LongRunningTaskTracker create(String jobClass, String serverName, Setting<Boolean> shouldSaveLongRunningTasks,
			LongRunningTaskType type){
		LongRunningTask task = new LongRunningTask(jobClass, serverName, type);
		return new LongRunningTaskTracker(longRunningTaskNode, task, shouldSaveLongRunningTasks);
	}
}
