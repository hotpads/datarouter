package com.hotpads.joblet;

import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.joblet.databean.Joblet;
import com.hotpads.joblet.databean.JobletData;
import com.hotpads.joblet.databean.JobletDataKey;
import com.hotpads.joblet.databean.JobletKey;
import com.hotpads.joblet.databean.JobletQueue;
import com.hotpads.joblet.databean.JobletQueueKey;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;

public interface JobletNodes{

	IndexedSortedMapStorageNode<JobletKey,Joblet> joblet();
	SortedMapStorage<JobletDataKey,JobletData> jobletData();
	SortedMapStorage<JobletQueueKey,JobletQueue> jobletQueue();

}
