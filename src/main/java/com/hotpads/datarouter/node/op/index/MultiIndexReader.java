package com.hotpads.datarouter.node.op.index;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.multi.MultiIndexEntry;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.iterable.SortedScannerIterable;

public interface MultiIndexReader<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends MultiIndexEntry<IK, IE, PK, D>>{

	List<D> lookupMulti(IK indexKey, boolean wildcardLastField, Config config);
	List<D> lookupMultiMulti(Collection<IK> indexKeys, boolean wildcardLastField, Config config);
	
	SortedScannerIterable<IE> scan(Range<IK> range, Config config);
	SortedScannerIterable<IK> scanKeys(Range<IK> range, Config config);
	SortedScannerIterable<D> scanDatabeans(Range<IK> range, Config config);
	
}
