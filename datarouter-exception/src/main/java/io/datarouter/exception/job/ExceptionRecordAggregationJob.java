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
package io.datarouter.exception.job;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import io.datarouter.exception.storage.exceptionrecord.DatarouterExceptionRecordDao;
import io.datarouter.exception.storage.exceptionrecord.ExceptionRecord;
import io.datarouter.exception.storage.exceptionrecord.ExceptionRecordKey;
import io.datarouter.exception.storage.summary.DatarouterExceptionRecordSummaryDao;
import io.datarouter.exception.storage.summary.ExceptionRecordSummary;
import io.datarouter.exception.storage.summary.ExceptionRecordSummaryKey;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.tuple.Range;

public class ExceptionRecordAggregationJob extends BaseJob{

	private static final long PERIOD_MS = Duration.ofHours(1).toMillis();

	@Inject
	private DatarouterExceptionRecordSummaryDao exceptionRecordSummaryDao;
	@Inject
	private DatarouterExceptionRecordDao exceptionRecordDao;

	@Override
	public void run(TaskTracker tracker){
		long now = System.currentTimeMillis();
		long beginningOfCurrentHour = now - now % PERIOD_MS;
		long lastPeriodToAggregate = beginningOfCurrentHour - PERIOD_MS;
		long firstPeriodToAggregate = exceptionRecordSummaryDao.scanKeys(1)
				.findFirst()
				.map(ExceptionRecordSummaryKey::getPeriodStart)
				.map(lastAggregatedPeriodStart -> lastAggregatedPeriodStart + PERIOD_MS)
				.orElse(lastPeriodToAggregate);

		for(long periodStart = firstPeriodToAggregate; periodStart <= lastPeriodToAggregate; periodStart += PERIOD_MS){
			aggregateOnePeriod(periodStart);
		}
	}

	private void aggregateOnePeriod(long periodStart){
		String startId = String.valueOf(periodStart);
		String endId = String.valueOf(periodStart + PERIOD_MS);
		Range<ExceptionRecordKey> range = new Range<>(new ExceptionRecordKey(startId), new ExceptionRecordKey(endId));

		Map<ExceptionRecordSummaryKey,Long> summaryCounts = new HashMap<>();
		Map<ExceptionRecordSummaryKey,String> sampledRecordIds = new HashMap<>();
		for(ExceptionRecord record : exceptionRecordDao.scan(range).iterable()){
			String exceptionLocation = Optional.ofNullable(record.getExceptionLocation())
					.orElse("");
			String type = Optional.ofNullable(record.getType())
					.orElse("");
			ExceptionRecordSummaryKey summaryKey = new ExceptionRecordSummaryKey(periodStart, type, exceptionLocation);
			summaryCounts.merge(summaryKey, 1L, Long::sum);
			sampledRecordIds.putIfAbsent(summaryKey, record.getKey().getId());
		}

		Scanner.of(summaryCounts.entrySet())
				.map(entry -> new ExceptionRecordSummary(
						entry.getKey(),
						entry.getValue(),
						sampledRecordIds.get(entry.getKey())))
				.batch(100)
				.forEach(exceptionRecordSummaryDao::putMulti);
	}

}
