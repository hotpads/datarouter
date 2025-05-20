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

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.util.VacuumMetrics;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.retry.RetryableTool;

public abstract class BaseNodeVacuum<PK extends PrimaryKey<PK>,T>{
	private static final Logger logger = LoggerFactory.getLogger(BaseNodeVacuum.class);

	private final Scanner<T> scanner;
	private final int deleteBatchSize;
	private final Consumer<Collection<PK>> deleteConsumer;
	private final String nameForMetrics;
	private final boolean shouldUpdateTaskTracker;
	private final Predicate<T> shouldDelete;
	private final Threads threads;

	protected BaseNodeVacuum(
			Scanner<T> scanner,
			Consumer<Collection<PK>> deleteConsumer,
			int deleteBatchSize,
			String nameForMetrics,
			boolean shouldUpdateTaskTracker,
			Predicate<T> shouldDelete,
			Threads threads){
		this.scanner = scanner;
		this.deleteBatchSize = deleteBatchSize;
		this.deleteConsumer = deleteConsumer;
		this.nameForMetrics = nameForMetrics;
		this.shouldUpdateTaskTracker = shouldUpdateTaskTracker;
		this.shouldDelete = shouldDelete;
		this.threads = threads;
	}

	protected abstract PK getKey(T item);

	public void run(TaskTracker tracker){
		var numDeleted = new AtomicLong();
		scanner
				.batch(100)//batch for efficient monitoring
				.advanceUntil(_ -> tracker.shouldStop())
				.each(batch -> {
					VacuumMetrics.considered(nameForMetrics, batch.size());
					if(shouldUpdateTaskTracker){
						tracker.increment(batch.size());
						tracker.setLastItemProcessed(batch.getLast().toString());
					}
				})
				.concat(Scanner::of)
				.include(shouldDelete)
				.map(this::getKey)
				.batch(deleteBatchSize)
				.parallelUnordered(threads)
				.each(this::deleteWithRetries)
				.each(batch -> VacuumMetrics.deleted(nameForMetrics, batch.size()))
				.map(Collection::size)
				.forEach(numDeleted::addAndGet);
		logProgress(numDeleted.get(), tracker.getCount(), tracker.getLastItem());
	}

	private void deleteWithRetries(List<PK> keys){
		RetryableTool.tryNTimesWithBackoffUnchecked(
				() -> deleteConsumer.accept(keys),
				3,
				Duration.ofSeconds(1),
				true);
	}

	private void logProgress(long numDeleted, long numScanned, String lastItem){
		logger.info("deleted {}/{} through {}",
				NumberFormatter.addCommas(numDeleted),
				NumberFormatter.addCommas(numScanned),
				lastItem);
	}

	public abstract static class BaseNodeVacuumBuilder<
			PK extends PrimaryKey<PK>,
			T,
			C extends BaseNodeVacuumBuilder<PK,T,C>>{

		protected final String nameForMetrics;
		protected final Scanner<T> scanner;
		protected final Predicate<T> shouldDelete;
		protected final Consumer<Collection<PK>> deleteConsumer;
		protected boolean shouldUpdateTaskTracker;
		protected int deleteBatchSize;
		protected Threads threads;

		public BaseNodeVacuumBuilder(
				String nameForMetrics,
				Scanner<T> scanner,
				Predicate<T> shouldDelete,
				Consumer<Collection<PK>> deleteConsumer){
			this.nameForMetrics = nameForMetrics;
			this.scanner = scanner;
			this.shouldDelete = shouldDelete;
			this.deleteConsumer = deleteConsumer;
			shouldUpdateTaskTracker = true;
			deleteBatchSize = 100;
			threads = Threads.none();
		}

		protected abstract C self();

		public C withDeleteBatchSize(int batchSize){
			this.deleteBatchSize = batchSize;
			return self();
		}

		public C withThreads(Threads threads){
			this.threads = threads;
			return self();
		}

		public C disableTaskTrackerUpdates(){
			this.shouldUpdateTaskTracker = false;
			return self();
		}

	}

}
