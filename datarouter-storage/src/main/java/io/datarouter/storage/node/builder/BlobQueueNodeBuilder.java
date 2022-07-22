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
package io.datarouter.storage.node.builder;

import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.factory.QueueNodeFactory;
import io.datarouter.storage.node.op.raw.BlobQueueStorage.BlobQueueStorageNode;
import io.datarouter.storage.tag.Tag;

public class BlobQueueNodeBuilder{

	protected final Datarouter datarouter;
	protected final QueueNodeFactory queueNodeFactory;
	protected final ClientId clientId;
	protected final String queueName;

	protected String namespace;
	protected String queueUrl;
	protected Tag tag;

	public BlobQueueNodeBuilder(
			Datarouter datarouter,
			QueueNodeFactory queueNodeFactory,
			ClientId clientId,
			String queueName){
		this.datarouter = datarouter;
		this.queueNodeFactory = queueNodeFactory;
		this.clientId = clientId;
		this.queueName = queueName;
	}

	public BlobQueueNodeBuilder withNamespace(String namespace){
		this.namespace = namespace;
		return this;
	}

	public BlobQueueNodeBuilder withQueueUrl(String queueUrl){
		this.queueUrl = queueUrl;
		return this;
	}

	public BlobQueueNodeBuilder withTag(Tag tag){
		this.tag = tag;
		return this;
	}

	public BlobQueueStorageNode buildAndRegister(){
		return datarouter.register(build());
	}

	public BlobQueueStorageNode build(){
		return queueNodeFactory.createBlobQueueNode(clientId, queueName, namespace, queueUrl, tag);
	}

}