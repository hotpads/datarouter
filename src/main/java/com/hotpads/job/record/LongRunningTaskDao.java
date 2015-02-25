package com.hotpads.job.record;

import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage;

public interface LongRunningTaskDao{

	IndexedSortedMapStorage<LongRunningTaskKey,LongRunningTask> getNode();
	
}
