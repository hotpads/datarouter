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
package io.datarouter.virtualnode.redundant;

import java.util.List;

import io.datarouter.storage.node.op.raw.BlobQueueStorage.BlobQueueStorageNode;
import io.datarouter.storage.queue.BlobQueueMessage;
import io.datarouter.storage.queue.BlobQueueMessage.BlobQueueMessageFielder;
import io.datarouter.storage.queue.BlobQueueMessageKey;
import io.datarouter.virtualnode.redundant.base.BaseRedundantQueueNode;
import io.datarouter.virtualnode.redundant.mixin.RedundantBlobQueueStorageMixin;

public class RedundantBlobQueueStorageNode
extends BaseRedundantQueueNode<BlobQueueMessageKey,BlobQueueMessage,BlobQueueMessageFielder,BlobQueueStorageNode>
implements BlobQueueStorageNode, RedundantBlobQueueStorageMixin{

	private RedundantBlobQueueStorageNode(List<BlobQueueStorageNode> nodes){
		super(nodes.get(0), nodes);
	}

	public static BlobQueueStorageNode makeIfMulti(
					List<BlobQueueStorageNode> nodes){
		if(nodes.size() == 1){
			return nodes.get(0);
		}
		return new RedundantBlobQueueStorageNode(nodes);
	}

}
