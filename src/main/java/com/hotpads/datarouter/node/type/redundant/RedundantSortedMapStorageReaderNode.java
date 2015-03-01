package com.hotpads.datarouter.node.type.redundant;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.reader.SortedMapStorageReader.SortedMapStorageReaderNode;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader.SortedStorageReaderNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.iterable.SortedScannerIterable;

public class RedundantSortedMapStorageReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends SortedMapStorageReaderNode<PK,D>>
extends RedundantMapStorageReaderNode<PK,D,N>
implements SortedStorageReaderNode<PK,D>{
	
	public RedundantSortedMapStorageReaderNode(Class<D> databeanClass, Datarouter router,
			Collection<N> writeNodes, N readNode) {
		super(databeanClass, router, writeNodes, readNode);
	}

	/***************** SortedStorageReader ************************************/
	
	@Override
	public D getFirst(Config config) {
		return readNode.getFirst(config);
	}

	@Override
	public PK getFirstKey(Config config) {
		return readNode.getFirstKey(config);
	}

	@Override
	public List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config) {
		return readNode.getWithPrefix(prefix,wildcardLastField, config);
	}

	@Override
	public List<D> getWithPrefixes(Collection<PK> prefixes, boolean wildcardLastField, Config config) {
		return readNode.getWithPrefixes(prefixes, wildcardLastField, config);
	}
	
	@Override
	public SortedScannerIterable<PK> scanKeys(Range<PK> range, Config config){
		return readNode.scanKeys(range, config);
	};
	
	@Override
	public SortedScannerIterable<D> scan(Range<PK> range, Config config){
		return readNode.scan(range, config);
	};
	
}
