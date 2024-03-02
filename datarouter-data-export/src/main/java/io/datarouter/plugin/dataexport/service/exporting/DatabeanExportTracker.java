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
package io.datarouter.plugin.dataexport.service.exporting;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.KvString;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.plugin.dataexport.util.RateTracker;
import io.datarouter.scanner.Scanner;
import io.datarouter.types.Ulid;
import io.datarouter.util.Count;
import io.datarouter.util.number.NumberFormatter;

public record DatabeanExportTracker(
		DatabeanExportTrackerType type,
		Ulid exportId,
		String clientName,
		String tableName,
		int threadCount,
		Count databeanCount,
		AtomicInteger completedPartCount,
		AtomicInteger totalParts,
		AtomicInteger partId,
		SortedSet<Integer> activePartIds,
		RateTracker rateTracker,
		AtomicReference<PrimaryKey<?>> lastKey,
		Instant startTime,
		// Start logging after this has elapsed
		Duration logDelay){
	private static final Logger logger = LoggerFactory.getLogger(DatabeanExportTracker.class);

	public enum DatabeanExportTrackerType{
		TABLE,
		PART;
	}

	public DatabeanExportTracker(
			DatabeanExportTrackerType type,
			Ulid exportId,
			String clientName,
			String tableName,
			int threadCount,
			Duration logDelay){
		this(
				type,
				exportId,
				clientName,
				tableName,
				threadCount,
				new Count(),
				new AtomicInteger(),
				new AtomicInteger(),
				new AtomicInteger(),
				new ConcurrentSkipListSet<>(),
				new RateTracker(),
				new AtomicReference<>(),
				Instant.now(),
				logDelay);
	}

	public void logProgress(){
		// Log only the slow worker threads.
		if(Duration.between(startTime, Instant.now()).compareTo(logDelay) < 0){
			return;
		}
		String trackerType = String.join("-", "track", type.name().toLowerCase());
		var attributes = new KvString()
				.add("databeans", databeanCount.value(), NumberFormatter::addCommas)
				.add("perSec", rateTracker.perSecDisplay())
				.add("perSecAvg", rateTracker.perSecAvgDisplay())
				.add("partProgress", completedPartCount.get() + "/" + totalParts.get())
				.add("activeParts", Scanner.of(activePartIds).map(Number::toString).collect(Collectors.joining(",")))
				.add("exportId", String.join("/", exportId.toString(), clientName, tableName))
				.add("lastKey", Optional.ofNullable(lastKey.get()).map(PrimaryKey::toString).orElse("?"))
				.add("threads", threadCount, Number::toString);
		logger.warn("{} {}", trackerType, attributes);
		rateTracker.markLogged();
	}

}
