package com.hotpads.datarouter.node.op.raw.read;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.IndexedOps;
import com.hotpads.datarouter.node.op.NodeOps;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.util.core.collections.Range;

/**
 * Methods for reading from storage systems that provide secondary indexing.
 *
 * This storage may be deprecated in favor of MultiIndexReader.
 */
public interface IndexedStorageReader<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends NodeOps<PK,D>, IndexedOps<PK,D>{

	public static final String
		OP_lookupUnique = "lookupUnique",
		OP_lookupMultiUnique = "lookupMultiUnique",
		OP_lookup = "lookup",
		OP_lookupMulti = "lookupMulti",
		OP_getFromIndex = "getFromIndex",
		OP_getByIndex = "getByIndex",
		OP_getIndexRange = "getIndexRange",
		OP_getIndexKeyRange = "getIndexKeyRange",
		OP_scanIndex = "scanIndex",
		OP_scanIndexKeys = "scanIndexKeys"
		;


	D lookupUnique(UniqueKey<PK> uniqueKey, Config config);
	List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config);

	List<D> lookup(Lookup<PK> lookup, boolean wildcardLastField, Config config);
	List<D> lookupMulti(Collection<? extends Lookup<PK>> lookup, Config config);

	<IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK, IE>>
	List<IE> getMultiFromIndex(Collection<IK> keys, Config config, DatabeanFieldInfo<IK, IE, IF> indexEntryFieldInfo);

	<IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>>
	List<D> getMultiByIndex(Collection<IK> keys, Config config);

	<IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK, IE>>
	Iterable<IE> scanIndex(DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Range<IK> range,
			Config config);

	<IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK, IE>>
	Iterable<IK> scanIndexKeys(DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Range<IK> range,
			Config config);

	/*************** sub-interfaces ***********************/

	public interface IndexedStorageReaderNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>>
	extends Node<PK,D>, IndexedStorageReader<PK,D>{
	}


	public interface PhysicalIndexedStorageReaderNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>>
	extends PhysicalNode<PK,D>, IndexedStorageReaderNode<PK,D>{
	}
}
