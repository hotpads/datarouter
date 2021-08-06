/*
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

import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.key.unique.UniqueKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.combo.IndexedSortedMapStorage;
import io.datarouter.storage.serialize.fieldcache.IndexEntryFieldInfo;
import io.datarouter.virtualnode.writebehind.base.WriteWrapper;
import io.datarouter.virtualnode.writebehind.config.DatarouterVirtualNodeExecutors.DatarouterWriteBehindExecutor;
import io.datarouter.virtualnode.writebehind.config.DatarouterVirtualNodeExecutors.DatarouterWriteBehindScheduler;
import io.datarouter.virtualnode.writebehind.mixin.WriteBehindIndexedStorageWriterMixin;
import io.datarouter.virtualnode.writebehind.mixin.WriteBehindMapStorageWriterMixin;
import io.datarouter.virtualnode.writebehind.mixin.WriteBehindSortedStorageWriterMixin;

public class WriteBehindIndexedSortedMapStorageNode<
	PK extends PrimaryKey<PK>,
	D extends Databean<PK,D>,
	N extends IndexedSortedMapStorage<PK,D>>
extends WriteBehindIndexedMapStorageReaderNode<PK,D,N>
implements IndexedSortedMapStorage<PK,D>,
		WriteBehindMapStorageWriterMixin<PK,D,N>,
		WriteBehindSortedStorageWriterMixin<PK,D,N>{

	protected final WriteBehindIndexedStorageWriterMixin<PK,D,N> mixinIndexedWriteOps;

	public WriteBehindIndexedSortedMapStorageNode(
			DatarouterWriteBehindScheduler scheduler,
			DatarouterWriteBehindExecutor writeExecutor,
			N backingNode){
		super(scheduler, writeExecutor, backingNode);
		mixinIndexedWriteOps = new WriteBehindIndexedStorageWriterMixin<>(this);
	}

	@Override
	public void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
		mixinIndexedWriteOps.deleteUnique(uniqueKey, config);
	}

	@Override
	public void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		mixinIndexedWriteOps.deleteMultiUnique(uniqueKeys, config);
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>> void deleteByIndex(Collection<IK> keys, Config config,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		mixinIndexedWriteOps.deleteByIndex(keys, config, indexEntryFieldInfo);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean handleWriteWrapperInternal(WriteWrapper<?> writeWrapper){
		if(super.handleWriteWrapperInternal(writeWrapper)){
			return true;
		}
		if(WriteBehindMapStorageWriterMixin.super.handleWriteWrapperInternal(writeWrapper)){
			return true;
		}
		if(WriteBehindSortedStorageWriterMixin.super.handleWriteWrapperInternal(writeWrapper)){
			return true;
		}
		if(writeWrapper.getOp().equals(OP_deleteUnique)){
			backingNode.deleteMultiUnique((Collection<? extends UniqueKey<PK>>)writeWrapper.getObjects(), writeWrapper
					.getConfig());
		}else{
			return false;
		}
		return true;
	}

}
