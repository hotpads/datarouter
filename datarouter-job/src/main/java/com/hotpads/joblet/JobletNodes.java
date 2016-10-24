package com.hotpads.joblet;

import java.util.Map;

import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.datarouter.node.op.raw.QueueStorage;
import com.hotpads.joblet.databean.JobletData;
import com.hotpads.joblet.databean.JobletDataKey;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.databean.JobletRequestKey;

public interface JobletNodes{

	IndexedSortedMapStorageNode<JobletRequestKey,JobletRequest> jobletRequest();
	SortedMapStorage<JobletDataKey,JobletData> jobletData();
	Map<JobletRequestQueueKey,QueueStorage<JobletRequestKey,JobletRequest>> jobletRequestQueueByKey();

}
