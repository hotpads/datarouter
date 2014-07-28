package com.hotpads.job.record;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.setting.Setting;


@Singleton
public class LongRunningTaskTrackerFactory {

	@Inject
	private IndexedSortedMapStorageNode<LongRunningTaskKey,LongRunningTask> longRunningTaskNode;

	public LongRunningTaskTracker create(String jobClass, String serverName,
			Setting<Boolean> shouldSaveLongRunningTasks, LongRunningTaskType type){
		LongRunningTask task = new LongRunningTask(jobClass, serverName, type);
		return new LongRunningTaskTracker(longRunningTaskNode, task, shouldSaveLongRunningTasks);
	}
}
