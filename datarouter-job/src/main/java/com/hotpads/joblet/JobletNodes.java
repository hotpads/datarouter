package com.hotpads.joblet;

import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.databean.JobletData;
import com.hotpads.joblet.databean.JobletDataKey;
import com.hotpads.joblet.databean.JobletKey;
import com.hotpads.joblet.databean.JobletQueue;
import com.hotpads.joblet.databean.JobletQueueKey;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;

public interface JobletNodes{

	IndexedSortedMapStorageNode<JobletKey,JobletRequest> joblet();
	SortedMapStorage<JobletDataKey,JobletData> jobletData();
	SortedMapStorage<JobletQueueKey,JobletQueue> jobletQueue();

}
