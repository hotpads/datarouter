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

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import io.datarouter.virtualnode.writebehind.base.WriteWrapper;
import io.datarouter.virtualnode.writebehind.config.DatarouterVirtualNodeExecutors.DatarouterWriteBehindExecutor;
import io.datarouter.virtualnode.writebehind.config.DatarouterVirtualNodeExecutors.DatarouterWriteBehindScheduler;
import io.datarouter.virtualnode.writebehind.mixin.WriteBehindMapStorageWriterMixin;
import io.datarouter.virtualnode.writebehind.mixin.WriteBehindSortedStorageWriterMixin;

public class WriteBehindSortedMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends SortedMapStorage<PK,D>>
extends WriteBehindSortedMapStorageReaderNode<PK,D,N>
implements SortedMapStorage<PK,D>,
		WriteBehindMapStorageWriterMixin<PK,D,N>,
		WriteBehindSortedStorageWriterMixin<PK,D,N>{

	public WriteBehindSortedMapStorageNode(
			DatarouterWriteBehindScheduler scheduler,
			DatarouterWriteBehindExecutor writeExecutor,
			N backingNode){
		super(scheduler, writeExecutor, backingNode);
	}

	@Override
	public boolean handleWriteWrapperInternal(WriteWrapper<?> writeWrapper){
		return super.handleWriteWrapperInternal(writeWrapper)
				|| WriteBehindMapStorageWriterMixin.super.handleWriteWrapperInternal(writeWrapper)
				|| WriteBehindSortedStorageWriterMixin.super.handleWriteWrapperInternal(writeWrapper);
	}

}
