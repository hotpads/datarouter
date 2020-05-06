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
package io.datarouter.storage.node.type.index;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.multi.MultiIndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import io.datarouter.storage.node.op.raw.MapStorage;
import io.datarouter.storage.op.scan.ManagedIndexIndexToDatabeanScanner;
import io.datarouter.util.tuple.Range;

public class ManualMultiIndexNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends MultiIndexEntry<IK,IE,PK,D>>
implements MultiIndexNode<PK, D, IK, IE>{

	private MapStorage<PK,D> mainNode;
	private SortedMapStorage<IK,IE> indexNode;

	public ManualMultiIndexNode(MapStorage<PK,D> mainNode, SortedMapStorage<IK,IE> indexNode){
		this.mainNode = mainNode;
		this.indexNode = indexNode;
	}

	/*----------------------------- IndexReader -----------------------------*/

	@Override
	public List<D> lookupMulti(IK indexKey, Config config){
		if(indexKey == null){
			return Collections.emptyList();
		}
		//hard-coding startInclusive to true because it will usually be true on the first call,
		// but subsequent calls may want false, so consider adding as method param
		var indexKeyRange = new Range<>(indexKey, true, indexKey, true);
		return indexNode.scan(indexKeyRange, config)
				.map(IE::getTargetKey)
				.listTo(primaryKeys -> mainNode.getMulti(primaryKeys, config));
	}


	@Override
	public List<D> lookupMultiMulti(Collection<IK> indexKeys, Config config){
		if(indexKeys == null || indexKeys.isEmpty()){
			return Collections.emptyList();
		}
		return Scanner.of(indexKeys)
				.map(indexKey -> new Range<>(indexKey, true, indexKey, true))
				.concat(indexKeyRange -> indexNode.scan(indexKeyRange))
				.map(IE::getTargetKey)
				.listTo(primaryKeys -> mainNode.getMulti(primaryKeys, config));
	}

	@Override
	public Scanner<IE> scanMulti(Collection<Range<IK>> ranges, Config config){
		return indexNode.scanMulti(ranges, config);
	}

	@Override
	public Scanner<D> scanDatabeansMulti(Collection<Range<IK>> ranges, Config config){
		return new ManagedIndexIndexToDatabeanScanner<>(mainNode, scanMulti(ranges, config), config);
	}

	@Override
	public Scanner<IK> scanKeysMulti(Collection<Range<IK>> range, Config config){
		return indexNode.scanKeysMulti(range, config);
	}

}
