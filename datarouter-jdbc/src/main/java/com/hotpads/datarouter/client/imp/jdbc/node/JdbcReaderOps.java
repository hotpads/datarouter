package com.hotpads.datarouter.client.imp.jdbc.node;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.client.imp.jdbc.op.BaseJdbcOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetKeysOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetPrimaryKeyRangesOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetRangesOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcGetWithPrefixesOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcLookupOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.JdbcLookupUniqueOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.index.JdbcGetByIndexOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.index.JdbcGetIndexOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.index.JdbcManagedIndexGetKeyRangeOp;
import com.hotpads.datarouter.client.imp.jdbc.op.read.index.JdbcManagedIndexGetRangeOp;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.op.executor.impl.SessionExecutorImpl;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.collections.Range;

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
		JdbcGetOp<PK,D,F> op = new JdbcGetOp<>(node, fieldCodecFactory, opName, keys, Config.nullSafe(config));
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

	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<IE> getMultiFromIndex(Collection<IK> keys, Config config, DatabeanFieldInfo<IK, IE, IF> indexEntryFieldInfo){
		String opName = IndexedStorageReader.OP_getFromIndex;
		BaseJdbcOp<List<IE>> op = new JdbcGetIndexOp<>(node, fieldCodecFactory, config,
				indexEntryFieldInfo.getDatabeanSupplier(), indexEntryFieldInfo.getFielderSupplier(), keys);
		return new SessionExecutorImpl<>(op, getTraceName(opName)).call();
	}

	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>>
	List<D> getMultiByIndex(Collection<IK> keys, Config config){
		String opName = IndexedStorageReader.OP_getByIndex;
		BaseJdbcOp<List<D>> op = new JdbcGetByIndexOp<>(node, fieldCodecFactory, keys, false, config);
		return new SessionExecutorImpl<>(op, getTraceName(opName)).call();
	}

	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<IE> getIndexRange(Range<IK> range, Config config, DatabeanFieldInfo<IK, IE, IF> indexEntryFieldInfo){
		String opName = IndexedStorageReader.OP_getIndexRange;
		JdbcManagedIndexGetRangeOp<PK,D,IK,IE,IF> op = new JdbcManagedIndexGetRangeOp<>(node, fieldCodecFactory,
				indexEntryFieldInfo, range, config);
		return new SessionExecutorImpl<>(op, getTraceName(opName)).call();
	}

	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<IK> getIndexKeyRange(Range<IK> range, Config config, DatabeanFieldInfo<IK, IE, IF> indexEntryFieldInfo){
		String opName = IndexedStorageReader.OP_getIndexKeyRange;
		JdbcManagedIndexGetKeyRangeOp<PK,D,IK,IE,IF> op = new JdbcManagedIndexGetKeyRangeOp<>(node, fieldCodecFactory,
				indexEntryFieldInfo, range, config);
		return new SessionExecutorImpl<>(op, getTraceName(opName)).call();
	}

	/************************************ SortedStorageReader methods ****************************/

	public List<D> getWithPrefixes(final Collection<PK> prefixes, final boolean wildcardLastField,
			final Config config) {
		String opName = SortedStorageReader.OP_getWithPrefixes;
		JdbcGetWithPrefixesOp<PK,D,F> op = new JdbcGetWithPrefixesOp<>(node, fieldCodecFactory, prefixes,
				wildcardLastField, config);
		return new SessionExecutorImpl<>(op, getTraceName(opName)).call();
	}

	public List<PK> getKeysInRanges(Collection<Range<PK>> ranges, final Config config) {
		if(ranges.stream().allMatch(Range::isEmpty)){
			return Collections.emptyList();
		}
		String opName = SortedStorageReader.OP_getKeysInRange;
		JdbcGetPrimaryKeyRangesOp<PK,D,F> op = new JdbcGetPrimaryKeyRangesOp<>(node, fieldCodecFactory, ranges, config);
		List<PK> result = new SessionExecutorImpl<>(op, getTraceName(opName)).call();
		return result;
	}

	public List<D> getRanges(final Collection<Range<PK>> ranges, final Config config) {
		if(ranges.stream().allMatch(Range::isEmpty)){
			return Collections.emptyList();
		}
		String opName = SortedStorageReader.OP_getRange;
		JdbcGetRangesOp<PK,D,F> op = new JdbcGetRangesOp<>(node, fieldCodecFactory, ranges, config);
		return new SessionExecutorImpl<>(op, getTraceName(opName)).call();
	}

	/*********************** helper ******************************/

	public String getTraceName(String opName){
		return node.getName() + " " + opName;
	}
}
