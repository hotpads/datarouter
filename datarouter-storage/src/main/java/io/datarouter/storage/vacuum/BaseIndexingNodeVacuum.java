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
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.model.key.primary.RegularPrimaryKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.number.NumberFormatter;

public abstract class BaseIndexingNodeVacuum<PK extends RegularPrimaryKey<PK>,T>{
	private static final Logger logger = LoggerFactory.getLogger(BaseIndexingNodeVacuum.class);

	private final Scanner<T> scanner;
	private final int deleteBatchSize;
	private final Consumer<Collection<PK>> deleteConsumer;
	private final Optional<Integer> logBatchSize;
	private final Predicate<T> shouldDelete;

	protected BaseIndexingNodeVacuum(
			Scanner<T> scanner,
			Consumer<Collection<PK>> deleteConsumer,
			int deleteBatchSize,
			Optional<Integer> logBatchSize,
			Predicate<T> shouldDelete){
		this.scanner = scanner;
		this.deleteBatchSize = deleteBatchSize;
		this.deleteConsumer = deleteConsumer;
		this.logBatchSize = logBatchSize;
		this.shouldDelete = shouldDelete;
	}

	protected abstract PK getKey(T item);

	public void run(TaskTracker tracker){
		var numDeleted = new AtomicLong();
		scanner
				.advanceUntil($ -> tracker.shouldStop())
				.each($ -> tracker.increment())
				.each(item -> tracker.setLastItemProcessed(item.toString()))
				.each($ -> {
					if(logBatchSize.isPresent() && tracker.getCount() % logBatchSize.get() == 0){
						logProgress(numDeleted.get(), tracker.getCount(), tracker.getLastItem());
					}
				})
				.include(shouldDelete)
				.map(this::getKey)
				.batch(deleteBatchSize)
				.each(deleteConsumer::accept)
				.map(Collection::size)
				.forEach(numDeleted::addAndGet);
		logProgress(numDeleted.get(), tracker.getCount(), tracker.getLastItem());
	}

	private void logProgress(long numDeleted, long numScanned, String lastItem){
		logger.info("deleted {}/{} through {}",
				NumberFormatter.addCommas(numDeleted),
				NumberFormatter.addCommas(numScanned),
				lastItem);
	}

	public abstract static class BaseIndexingNodeVacuumBuilder<
			PK extends RegularPrimaryKey<PK>,
			T,
			C extends BaseIndexingNodeVacuumBuilder<PK,T,C>>{

		protected final Scanner<T> scanner;
		protected final Predicate<T> shouldDelete;
		protected final Consumer<Collection<PK>> deleteConsumer;
		protected int deleteBatchSize;
		protected Optional<Integer> logBatchSize;

		public BaseIndexingNodeVacuumBuilder(
				Scanner<T> scanner,
				Predicate<T> shouldDelete,
				Consumer<Collection<PK>> deleteConsumer){
			this.scanner = scanner;
			this.shouldDelete = shouldDelete;
			this.deleteConsumer = deleteConsumer;
			this.deleteBatchSize = 100;
			this.logBatchSize = Optional.empty();
		}

		protected abstract C self();

		public C withDeleteBatchSize(int batchSize){
			this.deleteBatchSize = batchSize;
			return self();
		}

		public C withLogBatchSize(int logBatchSize){
			this.logBatchSize = Optional.of(logBatchSize);
			return self();
		}

	}

}
