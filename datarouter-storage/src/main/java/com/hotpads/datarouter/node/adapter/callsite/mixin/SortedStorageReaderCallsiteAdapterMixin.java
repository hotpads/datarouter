package com.hotpads.datarouter.node.adapter.callsite.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.callsite.CallsiteAdapter;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader.SortedStorageReaderNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.collections.Range;

public interface SortedStorageReaderCallsiteAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends SortedStorageReaderNode<PK,D>>
extends SortedStorageReader<PK,D>, CallsiteAdapter{

	N getBackingNode();

	@Override
	@Deprecated
	default List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config){
		Config nullSafeConfig = Config.nullSafe(config).clone().setCallsite(getCallsite());
		long startNs = System.nanoTime();
		List<D> results = null;
		try{
			results = getBackingNode().getWithPrefix(prefix, wildcardLastField, nullSafeConfig);
			return results;
		}finally{
			recordCollectionCallsite(nullSafeConfig, startNs, results);
		}
	}

	@Override
	@Deprecated
	default List<D> getWithPrefixes(Collection<PK> prefixes, boolean wildcardLastField, Config config){
		Config nullSafeConfig = Config.nullSafe(config).clone().setCallsite(getCallsite());
		long startNs = System.nanoTime();
		List<D> results = null;
		try{
			results = getBackingNode().getWithPrefixes(prefixes, wildcardLastField, nullSafeConfig);
			return results;
		}finally{
			recordCollectionCallsite(nullSafeConfig, startNs, results);
		}
	}

	@Override
	default Iterable<PK> scanKeys(Range<PK> range, Config config){
		Config nullSafeConfig = Config.nullSafe(config).clone().setCallsite(getCallsite());
		long startNs = System.nanoTime();
		try{
			return getBackingNode().scanKeys(range, nullSafeConfig);
		}finally{
			recordCallsite(nullSafeConfig, startNs, 1);
		}
	}

	@Override
	default Iterable<PK> scanKeysMulti(Collection<Range<PK>> ranges, Config config){
		Config nullSafeConfig = Config.nullSafe(config).clone().setCallsite(getCallsite());
		long startNs = System.nanoTime();
		try{
			return getBackingNode().scanKeysMulti(ranges, nullSafeConfig);
		}finally{
			recordCallsite(nullSafeConfig, startNs, 1);
		}
	}

	@Override
	default Iterable<D> scan(Range<PK> range, Config config){
		Config nullSafeConfig = Config.nullSafe(config).clone().setCallsite(getCallsite());
		long startNs = System.nanoTime();
		try{
			return getBackingNode().scan(range, nullSafeConfig);
		}finally{
			recordCallsite(nullSafeConfig, startNs, 1);
		}
	}

	@Override
	default Iterable<D> scanMulti(Collection<Range<PK>> ranges, Config config){
		Config nullSafeConfig = Config.nullSafe(config).clone().setCallsite(getCallsite());
		long startNs = System.nanoTime();
		try{
			return getBackingNode().scanMulti(ranges, nullSafeConfig);
		}finally{
			recordCallsite(nullSafeConfig, startNs, 1);
		}
	}
}
