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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.index.multi.MultiIndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import io.datarouter.storage.node.op.raw.MapStorage;
import io.datarouter.storage.op.scan.ManagedIndexIndexToDatabeanScanner;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.iterable.IterableTool;
import io.datarouter.util.iterable.scanner.Scanner;
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

	//TODO should i be passing config options around blindly?
	//TODO need to watch out for offset/limit


	/*----------------------------- IndexReader -----------------------------*/

	@Override
	public List<D> lookupMulti(IK indexKey, Config config){
		if(indexKey == null){
			return new LinkedList<>();
		}
		//hard-coding startInclusive to true because it will usually be true on the first call,
		// but subsequent calls may want false, so consider adding as method param
		Range<IK> indexKeyRange = new Range<>(indexKey, true, indexKey, true);
		List<IE> indexEntries = ListTool.createArrayList(indexNode.scan(indexKeyRange, null));
		List<PK> primaryKeys = IterableTool.map(indexEntries, IE::getTargetKey);
		List<D> databeans = mainNode.getMulti(primaryKeys, config);
		return databeans;
	}


	@Override
	public List<D> lookupMultiMulti(Collection<IK> indexKeys, Config config){
		if(CollectionTool.isEmpty(indexKeys)){
			return new LinkedList<>();
		}
		List<IE> allIndexEntries = new ArrayList<>();
		for(IK indexKey : indexKeys){
			Range<IK> indexKeyRange = new Range<>(indexKey, true, indexKey, true);
			List<IE> indexEntries = ListTool.createArrayList(indexNode.scan(indexKeyRange, null));
			allIndexEntries.addAll(CollectionTool.nullSafe(indexEntries));
		}
		List<PK> primaryKeys = IterableTool.map(allIndexEntries, IE::getTargetKey);
		List<D> databeans = mainNode.getMulti(primaryKeys, config);
		return databeans;
	}

	@Override
	public Scanner<IE> scanMulti(Collection<Range<IK>> ranges, Config config){
		return indexNode.scanMulti(ranges, config);
	}

	@Override
	public Iterable<D> scanDatabeansMulti(Collection<Range<IK>> ranges, Config config){
		return new ManagedIndexIndexToDatabeanScanner<>(mainNode, scanMulti(ranges, config), config);
	}

	@Override
	public Scanner<IK> scanKeysMulti(Collection<Range<IK>> range, Config config){
		return indexNode.scanKeysMulti(range, config);
	}

}
