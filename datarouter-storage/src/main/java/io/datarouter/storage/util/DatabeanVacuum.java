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
package io.datarouter.storage.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import io.datarouter.util.number.NumberFormatter;

public class DatabeanVacuum<PK extends PrimaryKey<PK>, D extends Databean<PK, D>>{
	private static final Logger logger = LoggerFactory.getLogger(DatabeanVacuum.class);

	private SortedMapStorage<PK, D> storage;
	private Predicate<D> predicate;
	private int batchSize = 100;

	public DatabeanVacuum(SortedMapStorage<PK,D> storage, Predicate<D> predicate){
		this.storage = storage;
		this.predicate = predicate;
	}

	public DatabeanVacuum<PK,D> setStorage(SortedMapStorage<PK,D> storage){
		this.storage = storage;
		return this;
	}

	public DatabeanVacuum<PK,D> setBatchSize(int batchSize){
		this.batchSize = batchSize;
		return this;
	}

	public Void run(){
		int deletionCount = 0;
		int scanCount = 0;
		List<PK> pkBatch = new ArrayList<>();
		for(D databean : storage.scan(null, null)){
			scanCount++;
			if(predicate.test(databean)){
				pkBatch.add(databean.getKey());
			}
			if(pkBatch.size() >= batchSize){
				storage.deleteMulti(pkBatch, null);
				deletionCount += pkBatch.size();
				logger.debug("DatabeanVacuum deleted " + NumberFormatter.addCommas(deletionCount) + " of "
						+ NumberFormatter.addCommas(scanCount) + " " + storage);
				pkBatch = new ArrayList<>();
			}
		}
		storage.deleteMulti(pkBatch, null);
		deletionCount += pkBatch.size();
		logger.info("DatabeanVacuum deleted " + NumberFormatter.addCommas(deletionCount) + " of "
				+ NumberFormatter.addCommas(scanCount) + " " + storage);
		return null;
	}

}
