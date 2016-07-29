package com.hotpads.job.noop;

import javax.inject.Singleton;

import com.hotpads.datarouter.client.imp.noop.NoOpNode;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage;
import com.hotpads.job.record.LongRunningTask;
import com.hotpads.job.record.LongRunningTaskKey;
import com.hotpads.job.record.LongRunningTaskNodeProvider;

@Singleton
public class EmptyLongRunningTaskNodeProvider implements LongRunningTaskNodeProvider{

	@Override
	public IndexedSortedMapStorage<LongRunningTaskKey, LongRunningTask> get(){
		return new NoOpNode<>();
	}

}
