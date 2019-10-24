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

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.number.NumberFormatter;

public class DatabeanVacuum<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>{
	private static final Logger logger = LoggerFactory.getLogger(DatabeanVacuum.class);

	private final Scanner<D> scanner;
	private final Predicate<D> shouldDelete;
	private final int deleteBatchSize;
	private final Consumer<Collection<PK>> deleteConsumer;
	private final Optional<Integer> logBatchSize;

	private DatabeanVacuum(
			Scanner<D> scanner,
			Consumer<Collection<PK>> deleteConsumer,
			int deleteBatchSize,
			Predicate<D> shouldDelete,
			Optional<Integer> logBatchSize){
		this.scanner = scanner;
		this.shouldDelete = shouldDelete;
		this.deleteBatchSize = deleteBatchSize;
		this.deleteConsumer = deleteConsumer;
		this.logBatchSize = logBatchSize;
	}

	public void run(TaskTracker tracker){
		var numDeleted = new AtomicLong();
		scanner
				.advanceUntil($ -> tracker.shouldStop())
				.peek($ -> tracker.increment())
				.peek(databean -> tracker.setLastItemProcessed(databean.toString()))
				.peek($ -> {
					if(logBatchSize.isPresent() && tracker.getCount() % logBatchSize.get() == 0){
						logProgress(numDeleted.get(), tracker.getCount(), tracker.getLastItem());
					}
				})
				.include(shouldDelete)
				.map(Databean::getKey)
				.batch(deleteBatchSize)
				.peek(deleteConsumer::accept)
				.map(Collection::size)
				.forEach(numDeleted::addAndGet);
		logProgress(numDeleted.get(), tracker.getCount(), tracker.getLastItem());
	}

	private void logProgress(long numDeleted, long numScanned, String lastItem){
		logger.warn("deleted {}/{} through {}",
				NumberFormatter.addCommas(numDeleted),
				NumberFormatter.addCommas(numScanned),
				lastItem);
	}

	public static class DatabeanVacuumBuilder<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>{
		private Scanner<D> scanner;
		private Predicate<D> shouldDelete;
		private final Consumer<Collection<PK>> deleteConsumer;
		private int deleteBatchSize;
		private Optional<Integer> logBatchSize;

		public DatabeanVacuumBuilder(
				Scanner<D> scanner,
				Predicate<D> shouldDelete,
				Consumer<Collection<PK>> deleteConsumer){
			this.scanner = scanner;
			this.shouldDelete = shouldDelete;
			this.deleteConsumer = deleteConsumer;
			this.deleteBatchSize = 100;
			this.logBatchSize = Optional.empty();
		}

		public DatabeanVacuumBuilder<PK,D> deleteBatchSize(int batchSize){
			this.deleteBatchSize = batchSize;
			return this;
		}

		public DatabeanVacuumBuilder<PK,D> logBatchSize(int logBatchSize){
			this.logBatchSize = Optional.of(logBatchSize);
			return this;
		}

		public DatabeanVacuum<PK,D> build(){
			return new DatabeanVacuum<>(scanner, deleteConsumer, deleteBatchSize, shouldDelete, logBatchSize);
		}

	}

}
