package com.hotpads.profile.count;

import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.profile.count.databean.AvailableCounter;
import com.hotpads.profile.count.databean.Count;
import com.hotpads.profile.count.databean.key.AvailableCounterKey;
import com.hotpads.profile.count.databean.key.CountKey;

public interface CountersNodes {
	SortedMapStorageNode<AvailableCounterKey,AvailableCounter> getAvailableCounter();
	SortedMapStorageNode<CountKey,Count> getCount();
}
