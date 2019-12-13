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
package io.datarouter.virtualnode.writebehind.mixin;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.key.unique.UniqueKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.write.IndexedStorageWriter;
import io.datarouter.storage.node.type.index.ManagedNode;
import io.datarouter.virtualnode.writebehind.base.BaseWriteBehindNode;
import io.datarouter.virtualnode.writebehind.base.WriteWrapper;

public class WriteBehindIndexedStorageWriterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends IndexedStorageWriter<PK,D>>
implements IndexedStorageWriter<PK,D>{

	private final BaseWriteBehindNode<PK,D,N> node;

	public WriteBehindIndexedStorageWriterMixin(BaseWriteBehindNode<PK,D,N> node){
		this.node = node;
	}

	@Override
	public void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
		node.getQueue().offer(new WriteWrapper<>(OP_deleteUnique, Collections.singletonList(uniqueKey), config));
	}

	@Override
	public void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		node.getQueue().offer(new WriteWrapper<>(OP_deleteUnique, uniqueKeys, config));
	}

	@Override
	public <IK extends PrimaryKey<IK>> void deleteByIndex(Collection<IK> keys, Config config){
		node.getQueue().offer(new WriteWrapper<>(OP_deleteByIndex, keys, config));
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>,
			MN extends ManagedNode<PK,D,IK,IE,IF>>
	MN registerManaged(MN managedNode){
		throw new UnsupportedOperationException();
	}

	@Override
	public List<ManagedNode<PK,D,?,?,?>> getManagedNodes(){
		throw new UnsupportedOperationException();
	}

}
