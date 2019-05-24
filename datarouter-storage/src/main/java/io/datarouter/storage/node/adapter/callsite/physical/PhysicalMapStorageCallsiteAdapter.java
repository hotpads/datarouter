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
package io.datarouter.storage.node.adapter.callsite.physical;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.node.adapter.PhysicalAdapterMixin;
import io.datarouter.storage.node.adapter.callsite.BaseCallsiteAdapter;
import io.datarouter.storage.node.adapter.callsite.mixin.MapStorageReaderCallsiteAdapterMixin;
import io.datarouter.storage.node.adapter.callsite.mixin.MapStorageWriterCallsiteAdapterMixin;
import io.datarouter.storage.node.op.raw.MapStorage.PhysicalMapStorageNode;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;

public class PhysicalMapStorageCallsiteAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalMapStorageNode<PK,D,F>>
extends BaseCallsiteAdapter<PK,D,F,N>
implements PhysicalMapStorageNode<PK,D,F>,
		PhysicalAdapterMixin<PK,D,F,N>,
		MapStorageWriterCallsiteAdapterMixin<PK,D,F,N>,
		MapStorageReaderCallsiteAdapterMixin<PK,D,F,N>{

	public PhysicalMapStorageCallsiteAdapter(N backingNode){
		super(backingNode);
	}

	@Override
	public PhysicalDatabeanFieldInfo<PK,D,F> getFieldInfo(){
		return PhysicalAdapterMixin.super.getFieldInfo();
	}

}
