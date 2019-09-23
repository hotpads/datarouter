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
package io.datarouter.storage.op.scan;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.databean.DatabeanTool;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.scanner.BaseScanner;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.read.MapStorageReader;
import io.datarouter.util.iterable.IterableTool;

public class ManagedIndexIndexToDatabeanScanner<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK,IE,PK,D>>
extends BaseScanner<D>{

	private static final int DEFAULT_BATCH_SIZE = 100;

	private final MapStorageReader<PK,D> mainNode;
	private final Iterator<List<IE>> indexEntryBatchIterator;
	private final Config config;

	private Iterator<IE> indexEntryIterator;
	private Map<PK,D> keyToDatabeans;

	public ManagedIndexIndexToDatabeanScanner(MapStorageReader<PK,D> mainNode, Scanner<IE> indexScanner,
			Config config){
		this.mainNode = mainNode;
		this.config = config;
		int batchSize = config.optInputBatchSize().orElse(DEFAULT_BATCH_SIZE);
		this.indexEntryBatchIterator = indexScanner.batch(batchSize).iterator();
	}

	@Override
	public boolean advance(){
		if(indexEntryIterator == null || !indexEntryIterator.hasNext()){
			if(!doLoad()){
				return false;
			}
		}
		current = keyToDatabeans.get(indexEntryIterator.next().getTargetKey());
		return true;
	}

	private boolean doLoad(){
		if(!indexEntryBatchIterator.hasNext()){
			return false;
		}
		List<IE> indexEntryBatch = indexEntryBatchIterator.next();
		List<PK> primaryKeys = IterableTool.map(indexEntryBatch, IE::getTargetKey);
		List<D> databeans = mainNode.getMulti(primaryKeys, config);
		keyToDatabeans = DatabeanTool.getByKey(databeans);
		indexEntryIterator = indexEntryBatch.iterator();
		return true;
	}

}
