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
package io.datarouter.tasktracker.storage;

import java.util.List;

import io.datarouter.instrumentation.task.TaskTrackerBatchDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.tag.Tag;
import io.datarouter.tasktracker.storage.LongRunningTask.LongRunningTaskFielder;
import io.datarouter.util.tuple.Range;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class LongRunningTaskDao extends BaseDao{

	public record LongRunningTaskDaoParams(
			List<ClientId> clientIds){
	}

	private final SortedMapStorageNode<LongRunningTaskKey,LongRunningTask,LongRunningTaskFielder> node;

	@Inject
	public LongRunningTaskDao(Datarouter datarouter, NodeFactory nodeFactory, LongRunningTaskDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					SortedMapStorageNode<LongRunningTaskKey,LongRunningTask,LongRunningTaskFielder> node =
							nodeFactory.create(clientId, LongRunningTask::new, LongRunningTaskFielder::new)
							.withTag(Tag.DATAROUTER)
							.disableNodewatchPercentageAlert()
							.build();
					return node;
				})
				.listTo(RedundantSortedMapStorageNode::makeIfMulti);
		datarouter.register(node);
	}

	public SortedMapStorage<LongRunningTaskKey,LongRunningTask> getNode(){
		return node;
	}

	public Scanner<LongRunningTask> scan(){
		return node.scan();
	}

	public Scanner<LongRunningTask> scan(Range<LongRunningTaskKey> range){
		return node.scan(range);
	}

	public Scanner<LongRunningTask> scanWithPrefix(LongRunningTaskKey prefix){
		return node.scanWithPrefix(prefix);
	}

	public Scanner<LongRunningTaskKey> scanKeysWithPrefix(LongRunningTaskKey prefix){
		return node.scanKeysWithPrefix(prefix);
	}

	public void deleteBatched(Scanner<LongRunningTaskKey> keys){
		node.deleteBatched(keys);
	}

	public Scanner<TaskTrackerBatchDto> scanAll(int batchSize){
		// probably not necessary to scan the whole table and send everything
		// TODO figure out what data we want to send and add some filtering
		return node.scan()
				.map(LongRunningTask::toDto)
				.batch(batchSize)
				.map(TaskTrackerBatchDto::new);
	}

}
