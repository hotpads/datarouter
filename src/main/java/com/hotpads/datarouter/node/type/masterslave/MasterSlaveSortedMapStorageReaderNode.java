package com.hotpads.datarouter.node.type.masterslave;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.reader.SortedMapStorageReader.SortedMapStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.iterable.PeekableIterable;
import com.hotpads.util.core.iterable.scanner.iterable.SortedScannerIterable;

public class MasterSlaveSortedMapStorageReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends SortedMapStorageReaderNode<PK,D>>
extends MasterSlaveMapStorageReaderNode<PK,D,F,N>
implements SortedMapStorageReaderNode<PK,D>{
	
	public MasterSlaveSortedMapStorageReaderNode(Class<D> databeanClass, DataRouter router, N master,
			Collection<N> slaves){
		super(databeanClass, router, master, slaves);
	}
	
	public MasterSlaveSortedMapStorageReaderNode(Class<D> databeanClass, DataRouter router){
		super(databeanClass, router);
	}

	/***************** SortedStorageReader ************************************/
	
	@Override
	public D getFirst(Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? chooseSlave(config) : master;
		return node.getFirst(config);
	}

	@Override
	public PK getFirstKey(Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? chooseSlave(config) : master;
		return node.getFirstKey(config);
	}
	
	@Override
	public List<D> getPrefixedRange(PK prefix, boolean wildcardLastField, PK start, boolean startInclusive,
			Config config){
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? chooseSlave(config) : master;
		return node.getPrefixedRange(prefix, wildcardLastField, start, startInclusive, config);
	}

	@Override
	public List<PK> getKeysInRange(PK start, boolean startInclusive, PK end, boolean endInclusive, Config config){
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? chooseSlave(config) : master;
		return node.getKeysInRange(start, startInclusive, end, endInclusive, config);
	}

	@Override
	public List<D> getRange(PK start, boolean startInclusive, PK end, boolean endInclusive, Config config){
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? chooseSlave(config) : master;
		return node.getRange(start, startInclusive, end, endInclusive, config);
	}

	@Override
	public List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? chooseSlave(config) : master;
		return node.getWithPrefix(prefix,wildcardLastField, config);
	}

	@Override
	public List<D> getWithPrefixes(Collection<PK> prefixes, boolean wildcardLastField, Config config){
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? chooseSlave(config) : master;
		return node.getWithPrefixes(prefixes, wildcardLastField, config);
	}
	
	@Override
	public SortedScannerIterable<PK> scanKeys(PK startKey, boolean startInclusive, PK end, boolean endInclusive,
			Config config){
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? chooseSlave(config) : master;
		return node.scanKeys(startKey,startInclusive, end, endInclusive, config);
	};
	
	@Override
	public SortedScannerIterable<D> scan(PK startKey, boolean startInclusive, PK end, boolean endInclusive, 
			Config config){
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? chooseSlave(config) : master;
		return node.scan(startKey,startInclusive, end, endInclusive, config);
	};
	
}