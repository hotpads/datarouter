package com.hotpads.job.noop;

import javax.inject.Singleton;

import com.hotpads.datarouter.client.imp.noop.NoOpNode;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage;
import com.hotpads.job.record.LongRunningTask;
import com.hotpads.job.record.LongRunningTaskDao;
import com.hotpads.job.record.LongRunningTaskKey;

@Singleton
public class EmptyLongRunningTaskDao implements LongRunningTaskDao{

	@Override
	public IndexedSortedMapStorage<LongRunningTaskKey, LongRunningTask> getNode(){
		return new NoOpNode<LongRunningTaskKey, LongRunningTask>();
	}

}
