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
package io.datarouter.storage.vacuum;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;

public class DatabeanVacuum<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends BaseNodeVacuum<PK,D>{

	private DatabeanVacuum(
			Scanner<D> scanner,
			Consumer<Collection<PK>> deleteConsumer,
			int deleteBatchSize,
			Predicate<D> shouldDelete,
			Optional<Integer> logBatchSize,
			Threads threads){
		super(scanner, deleteConsumer, deleteBatchSize, logBatchSize, shouldDelete, threads);
	}

	@Override
	protected PK getKey(D databean){
		return databean.getKey();
	}

	public static class DatabeanVacuumBuilder<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends BaseNodeVacuumBuilder<PK,D,DatabeanVacuumBuilder<PK,D>>{

		public DatabeanVacuumBuilder(
				Scanner<D> scanner,
				Predicate<D> shouldDelete,
				Consumer<Collection<PK>> deleteConsumer){
			super(scanner, shouldDelete, deleteConsumer);
		}

		@Override
		protected DatabeanVacuumBuilder<PK,D> self(){
			return this;
		}

		public DatabeanVacuum<PK,D> build(){
			return new DatabeanVacuum<>(
					scanner,
					deleteConsumer,
					deleteBatchSize,
					shouldDelete,
					logBatchSize,
					threads);
		}

	}

}
