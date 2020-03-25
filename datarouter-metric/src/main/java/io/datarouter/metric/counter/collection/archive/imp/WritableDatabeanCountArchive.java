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
package io.datarouter.metric.counter.collection.archive.imp;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import io.datarouter.conveyor.message.ConveyorMessage;
import io.datarouter.instrumentation.count.AtomicCounter;
import io.datarouter.instrumentation.count.CountCollectorPeriod;
import io.datarouter.instrumentation.count.CountDto;
import io.datarouter.metric.counter.DatarouterCountPublisherDao;
import io.datarouter.metric.counter.collection.archive.BaseCountArchive;
import io.datarouter.metric.counter.collection.archive.WritableCountArchive;
import io.datarouter.metric.counter.setting.DatarouterCountSettingRoot;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.util.DateTool;
import io.datarouter.util.collection.MapTool;
import io.datarouter.util.string.StringTool;

public class WritableDatabeanCountArchive extends BaseCountArchive implements WritableCountArchive{
	private static final Logger logger = LoggerFactory.getLogger(WritableDatabeanCountArchive.class);

	// prevent the 5s counter from early flushing because of slight lag
	private static final long MIN_EARLY_FLUSH_PERIOD_MS = Duration.ofSeconds(20).toMillis();
	private static final long DISCARD_IF_OLDER_THAN = Duration.ofMinutes(5).toMillis();

	private final Long flushPeriodMs;
	private final DatarouterCountPublisherDao countPublisherDao;
	private final Gson gson;
	private final DatarouterCountSettingRoot settings;

	private AtomicCounter aggregator;
	private Long lastFlushMs;// need to anchor this to the period... currently floating

	public WritableDatabeanCountArchive(String sourceType, String source, Long periodMs, Long flushPeriodMs,
			DatarouterCountPublisherDao countPublisherDao, Gson gson,
			DatarouterCountSettingRoot settings){
		super(sourceType, source, periodMs);
		this.aggregator = new AtomicCounter(DateTool.getPeriodStart(periodMs), periodMs);
		this.flushPeriodMs = flushPeriodMs;
		this.lastFlushMs = System.currentTimeMillis();
		this.countPublisherDao = countPublisherDao;
		this.gson = gson;
		this.settings = settings;
	}

	@Override
	public void saveCounts(CountCollectorPeriod countMap){
		if(countMap.getStartTimeMs() < System.currentTimeMillis() - DISCARD_IF_OLDER_THAN){
			// don't let them build up in memory for too long (datastore may hiccup)
			logger.warn("databean count archive flushing too slowly, discarding countMap older than:{} ms",
					DISCARD_IF_OLDER_THAN);
			return;
		}
		if(!shouldFlush(countMap)){
			aggregator.merge(countMap);
			return;
		}

		AtomicCounter oldAggregator = aggregator;
		long periodStart = DateTool.getPeriodStart(countMap.getStartTimeMs(), periodMs);
		aggregator = new AtomicCounter(periodStart, periodMs);
		aggregator.merge(countMap);
		List<ConveyorMessage> countDtosToSave = new ArrayList<>();
		for(Entry<String,AtomicLong> entry : MapTool.nullSafe(oldAggregator.getCountByKey()).entrySet()){
			if(entry.getValue() == null || entry.getValue().longValue() == 0){
				continue;
			}
			Long startTimeMs = System.currentTimeMillis();
			String sanitizedName = sanitizeName(entry.getKey());
			CountDto dto = new CountDto(sanitizedName, serviceName, periodMs, new Date(periodStart), source, new Date(
					startTimeMs), entry.getValue().get());
			countDtosToSave.add(new ConveyorMessage(dto.name, gson.toJson(dto)));
		}
		if(!countDtosToSave.isEmpty() && settings.runCountsToSqs.get()){
			countPublisherDao.putMulti(countDtosToSave);
		}
		lastFlushMs = System.currentTimeMillis();
	}

	private static String sanitizeName(String name){
		String sanitized = StringTool.trimToSize(name, CommonFieldSizes.DEFAULT_LENGTH_VARCHAR);
		sanitized = StringTool.removeNonStandardCharacters(sanitized);
		return sanitized;
	}

	private boolean shouldFlush(CountCollectorPeriod countMap){
		long periodEndsMs = aggregator.getStartTimeMs() + periodMs;
		boolean newPeriod = countMap.getStartTimeMs() >= periodEndsMs;
		if(newPeriod){
			return true;
		}

		long flushWindowPaddingMs = 10 * 1000;// give the normal flusher a chance to trigger it
		long nextFlushMs = lastFlushMs + flushPeriodMs + flushWindowPaddingMs;
		long now = System.currentTimeMillis();
		if(periodMs > MIN_EARLY_FLUSH_PERIOD_MS && now > nextFlushMs){
			return true;
		}
		return false;
	}

}
