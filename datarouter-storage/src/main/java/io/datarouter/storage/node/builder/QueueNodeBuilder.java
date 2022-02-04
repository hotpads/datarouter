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
package io.datarouter.storage.node.builder;

import java.util.function.Supplier;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.factory.QueueNodeFactory;
import io.datarouter.storage.node.op.NodeOps;
import io.datarouter.storage.tag.Tag;

public abstract class QueueNodeBuilder<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{

	protected final Datarouter datarouter;
	protected final QueueNodeFactory queueNodeFactory;
	protected final ClientId clientId;
	protected final Supplier<D> databeanSupplier;
	protected final Supplier<F> fielderSupplier;

	protected String queueName;
	protected String namespace;
	protected String queueUrl;
	protected Tag tag;

	public QueueNodeBuilder(
			Datarouter datarouter,
			QueueNodeFactory queueNodeFactory,
			ClientId clientId,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier){
		this.datarouter = datarouter;
		this.queueNodeFactory = queueNodeFactory;
		this.clientId = clientId;
		this.databeanSupplier = databeanSupplier;
		this.fielderSupplier = fielderSupplier;
	}

	public QueueNodeBuilder<PK,D,F> withQueueName(String queueName){
		this.queueName = queueName;
		return this;
	}

	public QueueNodeBuilder<PK,D,F> withNamespace(String namespace){
		this.namespace = namespace;
		return this;
	}

	public QueueNodeBuilder<PK,D,F> withQueueUrl(String queueUrl){
		this.queueUrl = queueUrl;
		return this;
	}

	public QueueNodeBuilder<PK,D,F> withTag(Tag tag){
		this.tag = tag;
		return this;
	}

	public abstract <N extends NodeOps<PK,D>> N build();

	public <N extends NodeOps<PK,D>> N buildAndRegister(){
		return datarouter.register(build());
	}

}