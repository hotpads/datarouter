package com.hotpads.datarouter.node.type.writebehind;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.reader.SortedMapStorageReader.SortedMapStorageReaderNode;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.collections.Range;

public class WriteBehindSortedMapStorageReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends SortedMapStorageReaderNode<PK,D>>
extends WriteBehindMapStorageReaderNode<PK,D,N>
implements SortedMapStorageReaderNode<PK,D>{

	public WriteBehindSortedMapStorageReaderNode(Supplier<D> databeanSupplier, Router router, N backingNode){
		super(databeanSupplier, router, backingNode);
	}

	@Override
	@Deprecated
	public List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config) {
		return backingNode.getWithPrefix(prefix,wildcardLastField, config);
	}

	@Override
	@Deprecated
	public List<D> getWithPrefixes(Collection<PK> prefixes, boolean wildcardLastField, Config config) {
		return backingNode.getWithPrefixes(prefixes, wildcardLastField, config);
	}

	@Override
	public Iterable<PK> scanKeysMulti(Collection<Range<PK>> ranges, Config config){
		return backingNode.scanKeysMulti(ranges, config);
	}

	@Override
	public Iterable<D> scanMulti(Collection<Range<PK>> ranges, Config config){
		return backingNode.scanMulti(ranges, config);
	}

}
