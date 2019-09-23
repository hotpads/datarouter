/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.storage.node.op.raw.read;

import java.util.Collection;
import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.key.unique.UniqueKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.op.IndexedOps;
import io.datarouter.storage.node.op.NodeOps;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.serialize.fieldcache.IndexEntryFieldInfo;
import io.datarouter.util.tuple.Range;

/**
 * Methods for reading from storage systems that provide secondary indexing.
 */
public interface IndexedStorageReader<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends NodeOps<PK,D>, IndexedOps<PK,D>{

	public static final String OP_lookupUnique = "lookupUnique";
	public static final String OP_lookupMultiUnique = "lookupMultiUnique";
	public static final String OP_getFromIndex = "getFromIndex";
	public static final String OP_getByIndex = "getByIndex";
	public static final String OP_getIndexRange = "getIndexRange";
	public static final String OP_getIndexKeyRange = "getIndexKeyRange";
	public static final String OP_getByIndexRange = "getByIndexRange";
	public static final String OP_scanIndex = "scanIndex";
	public static final String OP_scanByIndex = "scanByIndex";
	public static final String OP_scanIndexKeys = "scanIndexKeys";


	D lookupUnique(UniqueKey<PK> uniqueKey, Config config);

	default D lookupUnique(UniqueKey<PK> uniqueKey){
		return lookupUnique(uniqueKey, new Config());
	}

	List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config);

	default List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys){
		return lookupMultiUnique(uniqueKeys, new Config());
	}

	/*------------ getMultiFromIndex -------------*/

	<IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<IE> getMultiFromIndex(
			Collection<IK> keys,
			Config config,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo);

	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<IE> getMultiFromIndex(
			Collection<IK> keys,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		return getMultiFromIndex(keys, new Config(), indexEntryFieldInfo);
	}

	/*------------ getMultiByIndex -------------*/

	<IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<D> getMultiByIndex(
			Collection<IK> keys,
			Config config,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo);

	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<D> getMultiByIndex(
			Collection<IK> keys,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		return getMultiByIndex(keys, new Config(), indexEntryFieldInfo);
	}

	/*------------ scanMultiIndex -------------*/

	<IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<IE> scanMultiIndex(
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo,
			Collection<Range<IK>> ranges,
			Config config);

	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<IE> scanMultiIndex(
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo,
			Collection<Range<IK>> ranges){
		return scanMultiIndex(indexEntryFieldInfo, ranges, new Config());
	}

	/*------------ scanMultiByIndex -------------*/

	<IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<D> scanMultiByIndex(
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo,
			Collection<Range<IK>> ranges,
			Config config);

	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<D> scanMultiByIndex(
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo,
			Collection<Range<IK>> ranges){
		return scanMultiByIndex(indexEntryFieldInfo, ranges, new Config());
	}

	/*------------ scanMultiIndexKeys -------------*/

	<IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<IK> scanMultiIndexKeys(
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo,
			Collection<Range<IK>> ranges,
			Config config);

	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<IK> scanMultiIndexKeys(
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo,
			Collection<Range<IK>> ranges){
		return scanMultiIndexKeys(indexEntryFieldInfo, ranges, new Config());
	}

	/*------------ sub-interfaces -------------*/

	public interface IndexedStorageReaderNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends Node<PK,D,F>, IndexedStorageReader<PK,D>{
	}


	public interface PhysicalIndexedStorageReaderNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends PhysicalNode<PK,D,F>, IndexedStorageReaderNode<PK,D,F>{
	}

}
