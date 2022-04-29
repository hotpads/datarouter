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

import java.util.List;
import java.util.Objects;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.adapter.PhysicalAdapterMixin;
import io.datarouter.storage.node.adapter.sanitization.QueueStorageWriterSanitizationAdapter;
import io.datarouter.storage.node.op.raw.GroupQueueStorage.PhysicalGroupQueueStorageNode;
import io.datarouter.storage.queue.GroupQueueMessage;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;

public class PhysicalGroupQueueStorageSanitizationAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalGroupQueueStorageNode<PK,D,F>>
extends QueueStorageWriterSanitizationAdapter<PK,D,F,N>
implements PhysicalGroupQueueStorageNode<PK,D,F>, PhysicalAdapterMixin<PK,D,F,N>{

	public PhysicalGroupQueueStorageSanitizationAdapter(N backingNode){
		super(backingNode);
	}

	@Override
	public GroupQueueMessage<PK, D> peek(Config config){
		Objects.requireNonNull(config);
		return backingNode.peek(config);
	}

	@Override
	public List<GroupQueueMessage<PK, D>> peekMulti(Config config){
		Objects.requireNonNull(config);
		return backingNode.peekMulti(config);
	}

	@Override
	public Scanner<GroupQueueMessage<PK, D>> peekUntilEmpty(Config config){
		Objects.requireNonNull(config);
		return backingNode.peekUntilEmpty(config);
	}

	@Override
	public List<D> pollMulti(Config config){
		Objects.requireNonNull(config);
		return backingNode.pollMulti(config);
	}

	@Override
	public PhysicalDatabeanFieldInfo<PK,D,F> getFieldInfo(){
		return PhysicalAdapterMixin.super.getFieldInfo();
	}

}
