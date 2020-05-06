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
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.read.MapStorageReader;
import io.datarouter.virtualnode.writebehind.base.BaseWriteBehindNode;
import io.datarouter.virtualnode.writebehind.base.WriteWrapper;
import io.datarouter.virtualnode.writebehind.config.DatarouterVirtualNodeExecutors.DatarouterWriteBehindExecutor;
import io.datarouter.virtualnode.writebehind.config.DatarouterVirtualNodeExecutors.DatarouterWriteBehindScheduler;

public class WriteBehindMapStorageReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends MapStorageReader<PK,D>>
extends BaseWriteBehindNode<PK,D,N>
implements MapStorageReader<PK,D>{

	public WriteBehindMapStorageReaderNode(
			DatarouterWriteBehindScheduler scheduler,
			DatarouterWriteBehindExecutor writeExecutor,
			N backingNode){
		super(scheduler, writeExecutor, backingNode);
	}

	@Override
	public boolean exists(PK key, Config config){
		return backingNode.exists(key, config);
	}

	@Override
	public D get(PK key, Config config){
		return backingNode.get(key, config);
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config config){
		return backingNode.getMulti(keys, config);
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config config){
		return backingNode.getKeys(keys, config);
	}

	@Override
	public boolean handleWriteWrapperInternal(WriteWrapper<?> writeWrapper){
		return false;
	}

}
