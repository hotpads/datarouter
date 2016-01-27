package com.hotpads.job.record;

import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage;

public interface LongRunningTaskNodeProvider{

	IndexedSortedMapStorage<LongRunningTaskKey,LongRunningTask> get();
	
}
