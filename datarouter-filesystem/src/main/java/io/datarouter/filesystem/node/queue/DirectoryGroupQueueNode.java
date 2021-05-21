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
package io.datarouter.filesystem.node.queue;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import io.datarouter.filesystem.raw.queue.DirectoryQueue;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.StringDatabeanCodec;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.GroupQueueStorage.PhysicalGroupQueueStorageNode;
import io.datarouter.storage.queue.BaseQueueMessage;
import io.datarouter.storage.queue.GroupQueueMessage;

public class DirectoryGroupQueueNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseDirectoryQueueNode<PK,D,F>
implements PhysicalGroupQueueStorageNode<PK,D,F>{

	private static final int MAX_MESSAGE_BYTES = 1024 * 1024;

	public DirectoryGroupQueueNode(
			DirectoryQueue directoryQueue,
			NodeParams<PK,D,F> params){
		super(directoryQueue, params);
	}

	/*------------- reader ------------*/

	@Override
	public GroupQueueMessage<PK,D> peek(Config config){
		Config limitedConfig = config.clone().setLimit(1);
		return peekMulti(limitedConfig).stream().findFirst().orElse(null);
	}

	@Override
	public List<GroupQueueMessage<PK, D>> peekMulti(Config config){
		int limit = config.findLimit().orElse(1);
		DatabeanFielder<PK,D> fielder = getFieldInfo().getSampleFielder();
		StringDatabeanCodec codec = getFieldInfo().getSampleFielder().getStringDatabeanCodec();
		Supplier<D> databeanSupplier = getFieldInfo().getDatabeanSupplier();
		return Scanner.generate(directoryQueue::peek)
				.limit(limit)
				.advanceWhile(Optional::isPresent)
				.map(Optional::get)
				.map(directoryQueueMessage -> {
					byte[] id = directoryQueueMessage.getBytesId();
					List<D> databeans = codec.fromStringMulti(directoryQueueMessage.content, fielder, databeanSupplier);
					return new GroupQueueMessage<>(id, databeans);
				})
				.list();
	}

	@Override
	public Scanner<GroupQueueMessage<PK,D>> peekUntilEmpty(Config config){
		return Scanner.generate(() -> peek(config))
				.advanceUntil(Objects::isNull);
	}

	/*------------- writer ------------*/

	@Override
	public void put(D databean, Config config){
		putMulti(List.of(databean), config);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		DatabeanFielder<PK,D> fielder = getFieldInfo().getSampleFielder();
		StringDatabeanCodec codec = getFieldInfo().getSampleFielder().getStringDatabeanCodec();
		List<byte[]> databeansAsBytes = Scanner.of(databeans)
				.map(databean -> codec.toBytes(databean, fielder))
				.list();
		List<List<byte[]>> groups = codec.makeGroups(databeansAsBytes, MAX_MESSAGE_BYTES);
		Scanner.of(groups)
				.map(codec::concatGroup)
				.forEach(directoryQueue::putMessage);
	}

	/*------------- reader/writer ------------*/

	@Override
	public List<D> pollMulti(Config config){
		List<GroupQueueMessage<PK,D>> results = peekMulti(config);
		Scanner.of(results)
				.map(BaseQueueMessage::getKey)
				.flush(keys -> ackMulti(keys, config));
		return Scanner.of(results)
				.map(GroupQueueMessage::getDatabeans)
				.concat(Scanner::of)
				.list();
	}

}
