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

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.MapStorage;
import io.datarouter.storage.node.op.raw.write.MapStorageWriter;
import io.datarouter.virtualnode.writebehind.WriteBehindNode;
import io.datarouter.virtualnode.writebehind.base.WriteWrapper;

public interface WriteBehindMapStorageWriterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends MapStorage<PK,D>>
extends MapStorageWriter<PK,D>, WriteBehindNode<PK,D,N>{

	@Override
	default void delete(PK key, Config config){
		getQueue().offer(new WriteWrapper<>(OP_delete, Collections.singletonList(key), config));
	}

	@Override
	default void deleteMulti(Collection<PK> keys, Config config){
		getQueue().offer(new WriteWrapper<>(OP_delete, keys, config));
	}

	@Override
	default void deleteAll(Config config){
		getQueue().offer(new WriteWrapper<>(OP_deleteAll, Collections.singletonList(new Object()), config));
	}

	@Override
	default void put(D databean, Config config){
		putMulti(Collections.singletonList(databean), config);
	}

	@Override
	default void putMulti(Collection<D> databeans, Config config){
		getQueue().offer(new WriteWrapper<>(OP_put, databeans, config));
	}

	@SuppressWarnings("unchecked")
	@Override
	default boolean handleWriteWrapperInternal(WriteWrapper<?> writeWrapper){
		if(writeWrapper.getOp().equals(OP_put)){
			getBackingNode().putMulti((Collection<D>)writeWrapper.getObjects(), writeWrapper.getConfig());
		}else if(writeWrapper.getOp().equals(OP_delete)){
			getBackingNode().deleteMulti((Collection<PK>)writeWrapper.getObjects(), writeWrapper.getConfig());
		}else if(writeWrapper.getOp().equals(OP_deleteAll)){
			getBackingNode().deleteAll(writeWrapper.getConfig());
		}else{
			return false;
		}
		return true;
	}

}