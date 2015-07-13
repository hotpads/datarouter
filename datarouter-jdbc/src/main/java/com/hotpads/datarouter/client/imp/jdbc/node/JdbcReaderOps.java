package com.hotpads.datarouter.client.imp.jdbc.node;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcCountOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetFirstKeyOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetFirstOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetKeysOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetPrimaryKeyRangeOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetRangeOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetWithPrefixesOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcLookupOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcLookupUniqueOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.index.JdbcGetByIndexOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.index.JdbcGetIndexOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.index.JdbcManagedIndexGetRangeOp;
import com.hotpads.datarouter.client.imp.jdbc.scan.JdbcIndexScanner;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.op.executor.impl.SessionExecutorImpl;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.BaseLookup;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.Scanner;
import com.hotpads.util.core.iterable.scanner.iterable.ScannerIterable;

public class JdbcReaderOps<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{

	public static final int DEFAULT_ITERATE_BATCH_SIZE = 1000;
	
	private final JdbcReaderNode<PK,D,F> node;
	private final JdbcFieldCodecFactory fieldCodecFactory;
	
	/******************************* constructors ************************************/

	public JdbcReaderOps(JdbcReaderNode<PK,D,F> node, JdbcFieldCodecFactory fieldCodecFactory){
		this.node = node;
		this.fieldCodecFactory = fieldCodecFactory;
	}

	/************************************ MapStorageReader methods ****************************/
	
	public List<D> getMulti(final Collection<PK> keys, final Config config) {
		String opName = MapStorageReader.OP_getMulti;
		JdbcGetOp<PK,D,F> op = new JdbcGetOp<>(node, fieldCodecFactory, opName, keys, config);
		List<D> results = new SessionExecutorImpl<>(op, getTraceName(opName)).call();
		return results;
	}
	
	public List<PK> getKeys(final Collection<PK> keys, final Config config) {
		String opName = MapStorageReader.OP_getKeys;
		JdbcGetKeysOp<PK,D,F> op = new JdbcGetKeysOp<>(node, fieldCodecFactory, opName, keys, config);
		List<PK> results = new SessionExecutorImpl<>(op, getTraceName(opName)).call();
		return results;
	}

	/************************************ IndexedStorageReader methods ****************************/
	
	public Long count(final Lookup<PK> lookup, final Config config) {
		String opName = IndexedStorageReader.OP_count;
		JdbcCountOp<PK,D,F> op = new JdbcCountOp<>(node, fieldCodecFactory, lookup, config);
		return new SessionExecutorImpl<>(op, getTraceName(opName)).call();
	}
	
	public D lookupUnique(final UniqueKey<PK> uniqueKey, final Config config){
		String opName = IndexedStorageReader.OP_lookupUnique;
		JdbcLookupUniqueOp<PK,D,F> op = new JdbcLookupUniqueOp<>(node, fieldCodecFactory, DrListTool
				.wrap(uniqueKey), config);
		List<D> result = new SessionExecutorImpl<>(op, getTraceName(opName)).call();
		if(DrCollectionTool.size(result)>1){
			throw new DataAccessException("found >1 databeans with unique index key="+uniqueKey);
		}
		return DrCollectionTool.getFirst(result);
	}

	public List<D> lookupMultiUnique(final Collection<? extends UniqueKey<PK>> uniqueKeys, final Config config){
		String opName = IndexedStorageReader.OP_lookupMultiUnique;
		if(DrCollectionTool.isEmpty(uniqueKeys)){
			return new LinkedList<>();
		}
		JdbcLookupUniqueOp<PK,D,F> op = new JdbcLookupUniqueOp<>(node, fieldCodecFactory, uniqueKeys, config);
		return new SessionExecutorImpl<>(op, getTraceName(opName)).call();
	}
	
	//TODO pay attention to wildcardLastField
	public List<D> lookup(final Lookup<PK> lookup, final boolean wildcardLastField, final Config config) {
		String opName = IndexedStorageReader.OP_lookup;
		JdbcLookupOp<PK,D,F> op = new JdbcLookupOp<>(node, fieldCodecFactory, DrListTool.wrap(lookup),
				wildcardLastField, config);
		return new SessionExecutorImpl<>(op, getTraceName(opName)).call();
	}
	
	public List<D> lookupMulti(final Collection<? extends Lookup<PK>> lookups, final Config config) {
		String opName = IndexedStorageReader.OP_lookupMulti;
		if(DrCollectionTool.isEmpty(lookups)){
			return new LinkedList<>();
		}
		JdbcLookupOp<PK,D,F> op = new JdbcLookupOp<>(node, fieldCodecFactory, lookups, false, config);
		return new SessionExecutorImpl<>(op, getTraceName(opName)).call();
	}
	
	public <L extends BaseLookup<PK>> ScannerIterable<L> scanIndex(Class<L> indexClass){
		Scanner<L> scanner = new JdbcIndexScanner<>(node, fieldCodecFactory, indexClass,
				getTraceName("scanIndex"));
		return new ScannerIterable<>(scanner);
	}
	
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>> 
	List<IE> getMultiFromIndex(Collection<IK> keys, Config config, DatabeanFieldInfo<IK, IE, IF> indexEntryFieldInfo){
		BaseJdbcOp<List<IE>> op = new JdbcGetIndexOp<>(node, fieldCodecFactory, config,
				indexEntryFieldInfo.getDatabeanClass(), indexEntryFieldInfo.getFielderClass(), keys);
		return new SessionExecutorImpl<>(op, IndexedStorageReader.OP_getFromIndex).call();
	}
	
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>> 
	List<D> getMultiByIndex(Collection<IK> keys, Config config){
		BaseJdbcOp<List<D>> op = new JdbcGetByIndexOp<>(node, fieldCodecFactory, keys, false, config);
		return new SessionExecutorImpl<>(op, IndexedStorageReader.OP_getByIndex).call();
	}
	
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>> 
	List<IE> getIndexRange(Range<IK> range, Config config, DatabeanFieldInfo<IK, IE, IF> indexEntryFieldInfo){
		JdbcManagedIndexGetRangeOp<PK,D,IK,IE,IF> op = new JdbcManagedIndexGetRangeOp<>(node, fieldCodecFactory, 
				indexEntryFieldInfo, range, config);
		return new SessionExecutorImpl<>(op, IndexedStorageReader.OP_getIndexRange).call();
	}
	
	/************************************ SortedStorageReader methods ****************************/

	public D getFirst(final Config config) {
		String opName = SortedStorageReader.OP_getFirst;
		JdbcGetFirstOp<PK,D,F> op = new JdbcGetFirstOp<>(node, fieldCodecFactory, config);
		return new SessionExecutorImpl<>(op, getTraceName(opName)).call();
	}
	
	public PK getFirstKey(final Config config) {
		String opName = SortedStorageReader.OP_getFirstKey;
		JdbcGetFirstKeyOp<PK,D,F> op = new JdbcGetFirstKeyOp<>(node, fieldCodecFactory, config);
		return new SessionExecutorImpl<>(op, getTraceName(opName)).call();
	}

	public List<D> getWithPrefixes(final Collection<PK> prefixes, final boolean wildcardLastField, 
			final Config config) {
		String opName = SortedStorageReader.OP_getWithPrefixes;
		JdbcGetWithPrefixesOp<PK,D,F> op = new JdbcGetWithPrefixesOp<>(node, fieldCodecFactory, prefixes,
				wildcardLastField, config);
		return new SessionExecutorImpl<>(op, getTraceName(opName)).call();
	}

	public List<PK> getKeysInRange(Range<PK> range, final Config config) {
		String opName = SortedStorageReader.OP_getKeysInRange;
		JdbcGetPrimaryKeyRangeOp<PK,D,F> op = new JdbcGetPrimaryKeyRangeOp<>(node, fieldCodecFactory, range,
				config);
		List<PK> result = new SessionExecutorImpl<>(op, getTraceName(opName)).call();
		return result;
	}
	
	public List<D> getRange(final Range<PK> range, final Config config) {
		String opName = SortedStorageReader.OP_getRange;
		JdbcGetRangeOp<PK,D,F> op = new JdbcGetRangeOp<>(node, fieldCodecFactory, range, config);
		List<D> result = new SessionExecutorImpl<>(op, getTraceName(opName)).call();
		return result;
	}
	
	/*********************** helper ******************************/
	
	public String getTraceName(String opName){
		return node.getName() + " " + opName;
	}
}
