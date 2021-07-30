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
import io.datarouter.model.index.unique.UniqueIndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.imp.TxnManagedUniqueIndexNode;
import io.datarouter.storage.node.op.combo.IndexedMapStorage;
import io.datarouter.storage.node.type.index.UniqueIndexNode;
import io.datarouter.storage.serialize.fieldcache.IndexEntryFieldInfo;

public class ManagedNodeBuilder<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends UniqueIndexEntry<IK,IE,PK,D>,
		IF extends DatabeanFielder<IK,IE>>{

	private final Supplier<IE> databeanSupplier;
	private final Supplier<IF> fielderSupplier;
	private final IndexedMapStorage<PK,D> backingNode;
	private String indexName;

	public ManagedNodeBuilder(
			Supplier<IK> indexEntryKeySupplier,
			Supplier<IE> databeanSupplier,
			Supplier<IF> fielderSupplier,
			IndexedMapStorage<PK,D> backingNode){
		this.databeanSupplier = databeanSupplier;
		this.fielderSupplier = fielderSupplier;
		this.backingNode = backingNode;
		this.indexName = indexEntryKeySupplier.get().getClass().getSimpleName();
	}

	public ManagedNodeBuilder<PK,D,IK,IE,IF> withTableName(String tableName){
		this.indexName = tableName;
		return this;
	}

	public UniqueIndexNode<PK,D,IK,IE> build(){
		IndexEntryFieldInfo<IK,IE,IF> fieldInfo = new IndexEntryFieldInfo<>(indexName, databeanSupplier,
				fielderSupplier);
		return backingNode.registerManaged(new TxnManagedUniqueIndexNode<>(backingNode, fieldInfo, indexName));
	}

}