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
package io.datarouter.metric.template;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import io.datarouter.instrumentation.metric.collector.MetricTemplateCollector;
import io.datarouter.instrumentation.metric.collector.MetricTemplateDto;
import io.datarouter.metric.template.MetricTemplatePublisher.PublishedMetricTemplate;
import io.datarouter.storage.cache.CaffeineLoadingCache;
import io.datarouter.storage.cache.CaffeineLoadingCache.CaffeineLoadingCacheBuilder;

public class DatarouterMetricTemplateCollector implements MetricTemplateCollector{

	private static final long FLUSH_MS = Duration.ofSeconds(5).toMillis();

	private final String serviceName;
	private final MetricTemplateBuffer metricTemplateBuffer;
	private final CaffeineLoadingCache<MetricTemplateDto,MetricTemplateDto> loadingCache;
	private final Supplier<Boolean> saveToBuffer;

	private final Object batchLock = new Object();
	private Set<MetricTemplateDto> batch;
	private long lastFlushMs;
	private long nextFlushMs;

	public DatarouterMetricTemplateCollector(
			String serviceName,
			MetricTemplateBuffer metricTemplateBuffer,
			Supplier<Boolean> saveToBuffer){
		this.serviceName = serviceName;
		this.metricTemplateBuffer = metricTemplateBuffer;
		this.saveToBuffer = saveToBuffer;

		this.loadingCache = new CaffeineLoadingCacheBuilder<MetricTemplateDto,MetricTemplateDto>()
				.withExpireTtl(Duration.ofHours(1))
				.withLoadingFunction(Function.identity())
				.build();

		this.batch = new HashSet<>();
		this.lastFlushMs = 0;
		this.nextFlushMs = System.currentTimeMillis() + FLUSH_MS;
	}

	public synchronized void flush(long flushingMs){
		if(flushingMs <= lastFlushMs || batch.isEmpty()){
			return;
		}

		lastFlushMs = flushingMs;
		nextFlushMs = flushingMs + FLUSH_MS;

		Set<MetricTemplateDto> snapshot;
		synchronized(batchLock){
			snapshot = batch;
			batch = new HashSet<>();
		}

		if(saveToBuffer.get()){
			List<PublishedMetricTemplate> templates = snapshot.stream()
					.map(template -> new PublishedMetricTemplate(serviceName, template))
					.toList();

			metricTemplateBuffer.offerMulti(templates);
		}
	}

	@Override
	public void stopAndFlushAll(){
		flush(nextFlushMs);
	}

	@Override
	public void add(MetricTemplateDto pattern){
		if(System.currentTimeMillis() >= nextFlushMs){
			flush(nextFlushMs);
		}

		if(!loadingCache.load(pattern)){
			synchronized(batchLock){
				batch.add(pattern);
			}
		}
	}

}
