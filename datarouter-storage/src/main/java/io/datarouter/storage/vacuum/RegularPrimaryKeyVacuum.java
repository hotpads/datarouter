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

import io.datarouter.model.key.primary.RegularPrimaryKey;
import io.datarouter.scanner.Scanner;

public class RegularPrimaryKeyVacuum<PK extends RegularPrimaryKey<PK>>
extends BaseIndexingNodeVacuum<PK,PK>{

	private RegularPrimaryKeyVacuum(
			Scanner<PK> scanner,
			Consumer<Collection<PK>> deleteConsumer,
			int deleteBatchSize,
			Predicate<PK> shouldDelete,
			Optional<Integer> logBatchSize){
		super(scanner, deleteConsumer, deleteBatchSize, logBatchSize, shouldDelete);
	}

	@Override
	protected PK getKey(PK pk){
		return pk;
	}

	public static class RegularPrimaryKeyVacuumBuilder<PK extends RegularPrimaryKey<PK>>
	extends BaseIndexingNodeVacuumBuilder<PK,PK,RegularPrimaryKeyVacuumBuilder<PK>>{

		public RegularPrimaryKeyVacuumBuilder(
				Scanner<PK> scanner,
				Predicate<PK> shouldDelete,
				Consumer<Collection<PK>> deleteConsumer){
			super(scanner, shouldDelete, deleteConsumer);
		}

		@Override
		protected RegularPrimaryKeyVacuumBuilder<PK> self(){
			return this;
		}

		public RegularPrimaryKeyVacuum<PK> build(){
			return new RegularPrimaryKeyVacuum<>(scanner, deleteConsumer, deleteBatchSize, shouldDelete, logBatchSize);
		}

	}

}
