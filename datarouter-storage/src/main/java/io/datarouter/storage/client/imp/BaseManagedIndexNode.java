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
package io.datarouter.storage.client.imp;

import java.util.Collection;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.combo.IndexedMapStorage;
import io.datarouter.storage.node.type.index.base.BaseManagedNode;
import io.datarouter.storage.serialize.fieldcache.IndexEntryFieldInfo;
import io.datarouter.util.tuple.Range;

public class BaseManagedIndexNode
		<PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK,IE,PK,D>,
		IF extends DatabeanFielder<IK,IE>>
extends BaseManagedNode<PK,D,IK,IE,IF>{

	public BaseManagedIndexNode(IndexedMapStorage<PK,D> node, IndexEntryFieldInfo<IK,IE,IF> fieldInfo, String name){
		super(node, fieldInfo, name);
	}

	public Scanner<IE> scanMulti(Collection<Range<IK>> ranges, Config config){
		return node.scanMultiIndex(fieldInfo, ranges, config);
	}

	public Scanner<IK> scanKeysMulti(Collection<Range<IK>> ranges, Config config){
		return node.scanMultiIndexKeys(fieldInfo, ranges, config);
	}

	public Scanner<D> scanDatabeansMulti(Collection<Range<IK>> ranges, Config config){
		return node.scanMultiByIndex(fieldInfo, ranges, config);
	}

}
