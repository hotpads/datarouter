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
package io.datarouter.storage.node.adapter.sanitization.physical;

import java.util.Optional;

import io.datarouter.bytes.Codec;
import io.datarouter.model.databean.EmptyDatabean;
import io.datarouter.model.databean.EmptyDatabean.EmptyDatabeanFielder;
import io.datarouter.model.key.EmptyDatabeanKey;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.adapter.PhysicalAdapterMixin;
import io.datarouter.storage.node.adapter.sanitization.BaseSanitizationAdapter;
import io.datarouter.storage.node.op.raw.BlobQueueStorage.PhysicalBlobQueueStorageNode;
import io.datarouter.storage.queue.BlobQueueMessage;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.util.Require;

public class PhysicalBlobQueueStorageSanitizationAdapter<T>
extends BaseSanitizationAdapter<EmptyDatabeanKey,EmptyDatabean,EmptyDatabeanFielder,PhysicalBlobQueueStorageNode<T>>
implements PhysicalBlobQueueStorageNode<T>,
		PhysicalAdapterMixin<EmptyDatabeanKey,EmptyDatabean,EmptyDatabeanFielder,PhysicalBlobQueueStorageNode<T>>{

	public PhysicalBlobQueueStorageSanitizationAdapter(PhysicalBlobQueueStorageNode<T> backingNode){
		super(backingNode);
	}

	@Override
	public int getMaxRawDataSize(){
		return backingNode.getMaxRawDataSize();
	}

	@Override
	public Codec<T,byte[]> getCodec(){
		return backingNode.getCodec();
	}

	@Override
	public void putRaw(byte[] data, Config config){
		Require.noNulls(data, config);
		backingNode.putRaw(data, config);
	}

	@Override
	public Optional<BlobQueueMessage<T>> peek(Config config){
		Require.noNulls(config);
		return backingNode.peek(config);
	}

	@Override
	public void ack(byte[] handle, Config config){
		Require.noNulls(handle, config);
		backingNode.ack(handle, config);
	}

	@Override
	public Optional<BlobQueueMessage<T>> poll(Config config){
		Require.noNulls(config);
		return backingNode.poll(config);
	}

	@Override
	public PhysicalDatabeanFieldInfo<EmptyDatabeanKey,EmptyDatabean,EmptyDatabeanFielder> getFieldInfo(){
		return PhysicalAdapterMixin.super.getFieldInfo();
	}

}
