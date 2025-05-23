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
package io.datarouter.storage.client.imp;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.unique.UniqueIndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.combo.IndexedMapStorage;
import io.datarouter.storage.node.type.index.ManagedUniqueIndexNode;
import io.datarouter.storage.serialize.fieldcache.IndexEntryFieldInfo;

public class NoTxnManagedUniqueIndexNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends UniqueIndexEntry<IK,IE,PK,D>,
		IF extends DatabeanFielder<IK,IE>>
extends BaseManagedIndexNode<PK,D,IK,IE,IF>
implements ManagedUniqueIndexNode<PK,D,IK,IE,IF>{

	public NoTxnManagedUniqueIndexNode(
			IndexedMapStorage<PK,D> node,
			IndexEntryFieldInfo<IK,IE,IF> fieldInfo,
			String name){
		super(node, fieldInfo, name);
	}

	@Override
	public Optional<IE> find(IK uniqueKey, Config config){
		return getMulti(Collections.singleton(uniqueKey), config).stream().findFirst();
	}

	@Override
	public List<IE> getMulti(Collection<IK> uniqueKeys, Config config){
		return node.getMultiFromIndex(uniqueKeys, config, indexEntryFieldInfo);
	}

	@Override
	public D lookupUnique(IK uniqueKey, Config config){
		IE indexEntry = get(uniqueKey, config);
		if(indexEntry == null){
			return null;
		}
		return node.get(indexEntry.getTargetKey(), config);
	}

	@Override
	public List<D> lookupMultiUnique(Collection<IK> keys, Config config){
		return Scanner.of(getMulti(keys, config))
				.map(IE::getTargetKey)
				.listTo(targetKeys -> node.getMulti(targetKeys, config));
	}

	@Override
	public void deleteUnique(IK uniqueKey, Config config){
		IE indexEntry = get(uniqueKey, config);
		if(indexEntry == null){
			return;
		}
		node.delete(indexEntry.getTargetKey(), config);
	}

	@Override
	public void deleteMultiUnique(Collection<IK> viewIndexKeys, Config config){
		Scanner.of(getMulti(viewIndexKeys, config))
				.map(IE::getTargetKey)
				.flush(targetKeys -> node.deleteMulti(targetKeys, config));
	}

	@Override
	public Scanner<IE> scanMulti(Collection<IK> uniqueKeys, Config config){
		return Scanner.of(node.getMultiFromIndex(uniqueKeys, config, indexEntryFieldInfo));
	}

	@Override
	public Scanner<D> scanLookupMultiUnique(Collection<IK> uniqueKeys, Config config){
		return Scanner.of(getMulti(uniqueKeys, config))
				.map(IE::getTargetKey)
				.listTo(targetKeys -> node.scanMulti(targetKeys, config));
	}

}
