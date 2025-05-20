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

import java.util.function.Supplier;

import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.factory.TallyNodeFactory;
import io.datarouter.storage.node.op.raw.TallyStorage.PhysicalTallyStorageNode;
import io.datarouter.storage.tag.Tag;
import io.datarouter.storage.tally.Tally;
import io.datarouter.storage.tally.Tally.TallyFielder;

public class TallyNodeBuilder{

	private final Datarouter datarouter;
	private final TallyNodeFactory nodeFactory;
	private final ClientId clientId;
	private final Supplier<Tally> databeanSupplier;
	private final Supplier<TallyFielder> fielderSupplier;

	private Supplier<String> versionSupplier;
	private String tableName;
	private Tag tag;

	public TallyNodeBuilder(
			Datarouter datarouter,
			TallyNodeFactory nodeFactory,
			ClientId clientId,
			Supplier<Tally> databeanSupplier,
			Supplier<TallyFielder> fielderSupplier){
		this.datarouter = datarouter;
		this.nodeFactory = nodeFactory;
		this.clientId = clientId;
		this.databeanSupplier = databeanSupplier;
		this.fielderSupplier = fielderSupplier;
	}

	public TallyNodeBuilder withSchemaVersion(String version){
		return withSchemaVersionSupplier(() -> version);
	}

	public TallyNodeBuilder withSchemaVersionSupplier(Supplier<String> versionSupplier){
		this.versionSupplier = versionSupplier;
		return this;
	}

	public TallyNodeBuilder withTableName(String tableName){
		this.tableName = tableName;
		return this;
	}

	public TallyNodeBuilder withTag(Tag tag){
		this.tag = tag;
		return this;
	}

	public PhysicalTallyStorageNode build(){
		return nodeFactory.createTallyNode(
				clientId,
				databeanSupplier,
				fielderSupplier,
				versionSupplier,
				tableName,
				tag);
	}

	public PhysicalTallyStorageNode buildAndRegister(){
		return datarouter.register(build());
	}

}
