package com.hotpads.datarouter.node.type.index;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.iterable.SortedScannerIterable;

public interface ManagedIndexNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK, D>, 
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK, IE, PK, D>,
		IF extends DatabeanFielder<IK, IE>>
extends ManagedNode<IK, IE ,IF>{

	SortedScannerIterable<IE> scanIndex(Range<IK> range, Config config);
	
	SortedScannerIterable<D> scan(Range<IK> range, Config config);
	
}
