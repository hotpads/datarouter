package com.hotpads.datarouter.node.op.index;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.unique.UniqueIndexEntry;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.stream.StreamTool;

public interface UniqueIndexReader<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends UniqueIndexEntry<IK, IE, PK, D>>
extends IndexReader<PK,D,IK,IE>{

	public static final String
		OP_lookupUnique = "lookupUnique",
		OP_lookupMultiUnique = "lookupMultiUnique";

	IE get(IK uniqueKey, Config config);
	List<IE> getMulti(Collection<IK> uniqueKeys, Config config);

	D lookupUnique(IK indexKey, Config config);
	List<D> lookupMultiUnique( final Collection<IK> uniqueKeys, final Config config);

	Iterable<D> scanDatabeans(Range<IK> range, Config config);

	default Stream<D> streamDatabeans(Range<IK> range, Config config){
		return StreamTool.stream(scanDatabeans(range, config));
	}
}
