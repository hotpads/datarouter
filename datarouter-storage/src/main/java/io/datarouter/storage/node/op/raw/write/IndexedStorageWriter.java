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
package io.datarouter.storage.node.op.raw.write;

import java.util.Collection;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.key.unique.UniqueKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.op.IndexedOps;
import io.datarouter.storage.node.op.NodeOps;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.serialize.fieldcache.IndexEntryFieldInfo;

/**
 * Methods for writing to storage systems that provide secondary indexing.
 */
public interface IndexedStorageWriter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends NodeOps<PK,D>, IndexedOps<PK,D>{

	String OP_deleteUnique = "deleteUnique";
	String OP_deleteMultiUnique = "deleteMultiUnique";
	String OP_deleteByIndex = "deleteByIndex";

	void deleteUnique(UniqueKey<PK> uniqueKey, Config config);

	default void deleteUnique(UniqueKey<PK> uniqueKey){
		deleteUnique(uniqueKey, new Config());
	}

	void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config);

	default void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys){
		deleteMultiUnique(uniqueKeys, new Config());
	}

	<IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	void deleteByIndex(
			Collection<IK> keys, Config config,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo);

	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	void deleteByIndex(
			Collection<IK> keys,
			IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		deleteByIndex(keys, new Config(), indexEntryFieldInfo);
	}

	/*---------------------------- sub-interfaces ---------------------------*/

	interface IndexedStorageWriterNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends Node<PK,D,F>, IndexedStorageWriter<PK,D>{
	}


	interface PhysicalIndexedStorageWriterNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends PhysicalNode<PK,D,F>, IndexedStorageWriterNode<PK,D,F>{
	}

}
