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
package io.datarouter.metric.gauge;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.datarouter.binarydto.dto.BinaryDto;
import io.datarouter.binarydto.dto.BinaryDtoField;
import io.datarouter.metric.service.AggregatedGaugesPublisher.MetricCollectorStats;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.Require;

public class GaugeBinaryDto extends BinaryDto<GaugeBinaryDto>{

	@BinaryDtoField(index = 0)
	public final String ulid;
	@BinaryDtoField(index = 1)
	public final String serviceName;
	@BinaryDtoField(index = 2)
	public final String serverName;
	@BinaryDtoField(index = 3)
	public final Long period;
	@BinaryDtoField(index = 4)
	public final List<SingleGaugeBinaryDto> metrics;

	public GaugeBinaryDto(
			String ulid,
			String serviceName,
			String serverName,
			Long period,
			List<SingleGaugeBinaryDto> metrics){
		this.ulid = Require.notBlank(ulid);
		this.serviceName = Require.notBlank(serviceName);
		this.serverName = Require.notBlank(serverName);
		this.period = Require.notNull(period);
		Require.isFalse(metrics.isEmpty());
		this.metrics = metrics;
	}

	public static List<GaugeBinaryDto> createSizedGaugeBinaryDtosFromMetricStats(
			String ulid,
			String serviceName,
			String serverName,
			Map<Long,Map<String,MetricCollectorStats>> metrics,
			int batchSize){
		return Scanner.of(metrics.keySet())
				.concat(period -> Scanner.of(metrics.get(period).entrySet())
						.map(SingleGaugeBinaryDto::createFromMetricStats)
						.batch(getLargestPeriodSizeFromMetricStats(metrics, batchSize))
						.map(metricBatch -> new GaugeBinaryDto(ulid, serviceName, serverName, period, metricBatch)))
						.list();
	}

	private static int getLargestPeriodSizeFromMetricStats(
			Map<Long,Map<String,MetricCollectorStats>> metrics,
			int batchSize){
		if(batchSize == Integer.MAX_VALUE){
			return Scanner.of(metrics.values())
					.map(Map::size)
					.findMax(Comparator.naturalOrder())
					.orElse(0);
		}
		return batchSize;
	}


	public static class SingleGaugeBinaryDto extends BinaryDto<SingleGaugeBinaryDto>{

		@BinaryDtoField(index = 0)
		public final String name;
		@BinaryDtoField(index = 1)
		public final Long sum;
		@BinaryDtoField(index = 2)
		public final Long count;
		@BinaryDtoField(index = 3)
		public final Long min;
		@BinaryDtoField(index = 4)
		public final Long max;

		public SingleGaugeBinaryDto(String name, Long sum, Long count, Long min, Long max){
			this.name = name;
			this.sum = sum;
			this.count = count;
			this.min = min;
			this.max = max;
		}

		private static SingleGaugeBinaryDto createFromMetricStats(Entry<String,MetricCollectorStats> entry){
			Require.notNull(entry.getValue());
			var name = Require.notNull(entry.getKey());
			var sum = Require.notNull(entry.getValue().sum());
			var count = Require.greaterThan(Require.notNull(entry.getValue().count()), 0L, name);
			var min = Require.notNull(entry.getValue().min());
			var max = Require.notNull(entry.getValue().max());
			return new SingleGaugeBinaryDto(name, sum, count, min, max);
		}
	}

}