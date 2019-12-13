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
package io.datarouter.virtualnode.writebehind;

import java.util.Collection;
import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.key.unique.UniqueKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.combo.reader.IndexedSortedMapStorageReader;
import io.datarouter.storage.node.type.index.ManagedNode;
import io.datarouter.storage.serialize.fieldcache.IndexEntryFieldInfo;
import io.datarouter.util.tuple.Range;

public class WriteBehindIndexedMapStorageReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends IndexedSortedMapStorageReader<PK,D>>
extends WriteBehindSortedMapStorageReaderNode<PK,D,N>
implements IndexedSortedMapStorageReader<PK,D>{

	public WriteBehindIndexedMapStorageReaderNode(Datarouter datarouter, N backingNode){
		super(datarouter, backingNode);
	}

	@Override
	public D lookupUnique(UniqueKey<PK> uniqueKey, Config config){
		return backingNode.lookupUnique(uniqueKey, config);
	}

	@Override
	public List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		return backingNode.lookupMultiUnique(uniqueKeys, config);
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<IE> getMultiFromIndex(Collection<IK> keys, Config config, IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		return backingNode.getMultiFromIndex(keys, config, indexEntryFieldInfo);
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	List<D> getMultiByIndex(Collection<IK> keys, Config config, IndexEntryFieldInfo<IK, IE, IF> indexEntryFieldInfo){
		return backingNode.getMultiByIndex(keys, config, indexEntryFieldInfo);
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<IE> scanMultiIndex(
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo,
			Collection<Range<IK>> ranges,
			Config config){
		return backingNode.scanMultiIndex(indexEntryFieldInfo, ranges, config);
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<D> scanMultiByIndex(
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo,
			Collection<Range<IK>> ranges,
			Config config){
		return backingNode.scanMultiByIndex(indexEntryFieldInfo, ranges, config);
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Scanner<IK> scanMultiIndexKeys(
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo,
			Collection<Range<IK>> ranges,
			Config config){
		return backingNode.scanMultiIndexKeys(indexEntryFieldInfo, ranges, config);
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>,
			MN extends ManagedNode<PK,D,IK,IE,IF>>
	MN registerManaged(MN managedNode){
		return backingNode.registerManaged(managedNode);
	}

	@Override
	public List<ManagedNode<PK,D,?,?,?>> getManagedNodes(){
		return backingNode.getManagedNodes();
	}

}
