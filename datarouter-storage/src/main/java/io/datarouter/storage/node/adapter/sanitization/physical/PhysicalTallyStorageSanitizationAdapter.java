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
package io.datarouter.storage.node.adapter.sanitization.physical;

import io.datarouter.storage.node.adapter.PhysicalAdapterMixin;
import io.datarouter.storage.node.adapter.sanitization.TallyStorageSanitizationAdapter;
import io.datarouter.storage.node.op.raw.TallyStorage.PhysicalTallyStorageNode;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.storage.tally.Tally;
import io.datarouter.storage.tally.Tally.TallyFielder;
import io.datarouter.storage.tally.TallyKey;

public class PhysicalTallyStorageSanitizationAdapter
extends TallyStorageSanitizationAdapter
implements PhysicalTallyStorageNode,
		PhysicalAdapterMixin<TallyKey,Tally,TallyFielder,PhysicalTallyStorageNode>{

	public PhysicalTallyStorageSanitizationAdapter(PhysicalTallyStorageNode backingNode){
		super(backingNode);
	}

	@Override
	public PhysicalDatabeanFieldInfo<TallyKey,Tally,TallyFielder> getFieldInfo(){
		return PhysicalAdapterMixin.super.getFieldInfo();
	}

}
