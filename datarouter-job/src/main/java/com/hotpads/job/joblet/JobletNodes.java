package com.hotpads.job.joblet;

import com.hotpads.config.job.databean.Joblet;
import com.hotpads.config.job.databean.JobletData;
import com.hotpads.config.job.databean.JobletDataKey;
import com.hotpads.config.job.databean.JobletKey;
import com.hotpads.config.job.databean.JobletQueue;
import com.hotpads.config.job.databean.JobletQueueKey;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;

public interface JobletNodes{

	IndexedSortedMapStorageNode<JobletKey,Joblet> joblet();
	SortedMapStorage<JobletDataKey,JobletData> jobletData();
	SortedMapStorage<JobletQueueKey,JobletQueue> jobletQueue();

}
