package com.hotpads.job.record;

import javax.inject.Singleton;

import com.hotpads.datarouter.client.imp.noop.NoOpNode;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage;

@Singleton
public class EmptyLongRunningTaskDao implements LongRunningTaskDao{

	@Override
	public IndexedSortedMapStorage<LongRunningTaskKey, LongRunningTask> getNode(){
		return new NoOpNode<LongRunningTaskKey, LongRunningTask>();
	}

}
