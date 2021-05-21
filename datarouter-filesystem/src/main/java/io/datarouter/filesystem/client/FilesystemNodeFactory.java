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
package io.datarouter.filesystem.client;

import javax.inject.Singleton;

import io.datarouter.filesystem.node.queue.DirectoryGroupQueueNode;
import io.datarouter.filesystem.node.queue.DirectoryQueueNode;
import io.datarouter.filesystem.raw.queue.DirectoryQueue;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.node.NodeParams;

@Singleton
public class FilesystemNodeFactory{

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	DirectoryQueueNode<PK,D,F> createSingleNode(DirectoryQueue directoryQueue, NodeParams<PK,D,F> params){
		return new DirectoryQueueNode<>(
				directoryQueue,
				params);
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	DirectoryGroupQueueNode<PK,D,F> createGroupNode(DirectoryQueue directoryQueue, NodeParams<PK,D,F> params){
		return new DirectoryGroupQueueNode<>(
				directoryQueue,
				params);
	}

}
