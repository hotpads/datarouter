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
package io.datarouter.storage.node.adapter.callsite;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.node.adapter.callsite.mixin.IndexedStorageCallsiteAdapterMixin;
import io.datarouter.storage.node.adapter.callsite.mixin.MapStorageReaderCallsiteAdapterMixin;
import io.datarouter.storage.node.adapter.callsite.mixin.MapStorageWriterCallsiteAdapterMixin;
import io.datarouter.storage.node.adapter.callsite.mixin.SortedStorageReaderCallsiteAdapterMixin;
import io.datarouter.storage.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import io.datarouter.storage.node.op.combo.IndexedSortedMapStorage.PhysicalIndexedSortedMapStorageNode;

public class IndexedSortedMapStorageCallsiteAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalIndexedSortedMapStorageNode<PK,D,F>>
extends BaseCallsiteAdapter<PK,D,F,N>
implements IndexedSortedMapStorageNode<PK,D,F>,
		MapStorageWriterCallsiteAdapterMixin<PK,D,F,N>,
		MapStorageReaderCallsiteAdapterMixin<PK,D,F,N>,
		SortedStorageReaderCallsiteAdapterMixin<PK,D,F,N>,
		IndexedStorageCallsiteAdapterMixin<PK,D,F,N>{

	public IndexedSortedMapStorageCallsiteAdapter(N backingNode){
		super(backingNode);
	}

}
