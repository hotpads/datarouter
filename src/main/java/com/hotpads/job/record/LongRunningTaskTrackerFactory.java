package com.hotpads.job.record;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.job.record.LongRunningTaskTracker.LongRunningTaskNode;


@Singleton
public class LongRunningTaskTrackerFactory {

	private IndexedSortedMapStorageNode longRunningTaskNode;
	
	@Inject
	public LongRunningTaskTrackerFactory(@LongRunningTaskNode IndexedSortedMapStorageNode longRunningTaskNode){
		this.longRunningTaskNode = longRunningTaskNode;
	}
	
	public LongRunningTaskTracker createLongRunningTaskTracker(String jobClass, String serverName){
		LongRunningTask task = new LongRunningTask(jobClass, serverName);
		return new LongRunningTaskTracker(longRunningTaskNode, task);
	}
}
