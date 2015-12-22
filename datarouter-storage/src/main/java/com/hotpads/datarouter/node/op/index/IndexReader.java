package com.hotpads.datarouter.node.op.index;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.util.core.exception.NotImplementedException;

public interface IndexReader<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK, IE, PK, D>>
extends SortedStorageReader<IK,IE>{

	/**
	 * @deprecated see SortedStorageReader.getWithPrefix
	 */
	@Override
	@Deprecated
	default List<IE> getWithPrefix(IK prefix, boolean wildcardLastField, Config config){
		throw new NotImplementedException();
	}

	/**
	 * @deprecated see SortedStorageReader.getWithPrefixes
	 */
	@Override
	@Deprecated
	default List<IE> getWithPrefixes(Collection<IK> prefixes, boolean wildcardLastField, Config config){
		throw new NotImplementedException();
	}

	@Override
	default Stream<IE> streamWithPrefixes(Collection<IK> prefixes, Config config){
		return prefixes.stream().flatMap(prefix -> streamWithPrefix(prefix, config));
	}

}
