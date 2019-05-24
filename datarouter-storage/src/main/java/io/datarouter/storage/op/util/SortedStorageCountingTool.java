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
package io.datarouter.storage.op.util;

import java.util.Optional;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader;
import io.datarouter.util.tuple.Range;

public class SortedStorageCountingTool{

	private static final int BATCH_SIZE = 10000;

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> long count(SortedStorageReader<PK,D> node,
			Range<PK> range){
		range = Range.nullSafe(range);
		PK startKey = null;
		long count = 0;
		for(PK key : node.scanKeys(range, new Config().setOutputBatchSize(BATCH_SIZE).setLimit(BATCH_SIZE))){
			startKey = key;
			count++;
		}
		if(count < BATCH_SIZE){
			return count;
		}
		Config batchConfig = new Config().setLimit(1).setOffset(BATCH_SIZE);
		Optional<PK> currentKey;
		do{
			Range<PK> batchRange = new Range<>(startKey, true, range.getEnd(), range.getEndInclusive());
			currentKey = node.streamKeys(batchRange, batchConfig).findFirst();
			if(currentKey.isPresent()){
				count += BATCH_SIZE;
				startKey = currentKey.get();
			}
		}while(currentKey.isPresent());
		return count += node.streamKeys(new Range<>(startKey, false, range.getEnd(), range.getEndInclusive()),
				new Config().setOutputBatchSize(BATCH_SIZE)).count();
	}

}
