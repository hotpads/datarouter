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
package io.datarouter.metric.counter;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.BinaryDto;
import io.datarouter.binarydto.dto.BinaryDtoField;
import io.datarouter.metric.counter.collection.DatarouterCountCollector.CountCollectorStats;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.Require;

public class CountBinaryDto extends BinaryDto<CountBinaryDto>{

	@BinaryDtoField(index = 0)
	public final String ulid;
	@BinaryDtoField(index = 1)
	public final String serviceName;
	@BinaryDtoField(index = 2)
	public final String serverName;
	@BinaryDtoField(index = 3)
	public final Long period;
	@BinaryDtoField(index = 4)
	public final List<SingleCountBinaryDto> counts;

	public CountBinaryDto(
			String ulid,
			String serviceName,
			String serverName,
			Long period,
			List<SingleCountBinaryDto> counts){
		this.ulid = Require.notBlank(ulid);
		this.serviceName = Require.notBlank(serviceName);
		this.serverName = Require.notBlank(serverName);
		this.period = Require.notNull(period);
		Require.isFalse(counts.isEmpty());
		this.counts = counts;
	}

	public static List<CountBinaryDto> createSizedCountBinaryDtos(String ulid, String serviceName, String serverName,
			Map<Long,Map<String,CountCollectorStats>> counts, int batchSize){
		return Scanner.of(counts.keySet())
				.concat(period -> Scanner.of(counts.get(period).entrySet())
						.map(SingleCountBinaryDto::createFromCountStats)
						.batch(getLargestPeriodSizeFromCountStats(counts, batchSize))
						.map(countBatch -> new CountBinaryDto(ulid, serviceName, serverName, period, countBatch)))
						.list();
	}

	private static int getLargestPeriodSizeFromCountStats(Map<Long,Map<String,CountCollectorStats>> counts,
			int batchSize){
		if(batchSize == Integer.MAX_VALUE){
			return Scanner.of(counts.values())
					.map(Map::size)
					.findMax(Comparator.naturalOrder())
					.orElse(0);
		}
		return batchSize;
	}

	public static class SingleCountBinaryDto extends BinaryDto<SingleCountBinaryDto>{

		@BinaryDtoField(index = 0)
		public final String name;
		@BinaryDtoField(index = 1)
		public final Long value; // sum
		@BinaryDtoField(index = 2)
		public final Long count;
		@BinaryDtoField(index = 3)
		public final Long min;
		@BinaryDtoField(index = 4)
		public final Long max;

		public SingleCountBinaryDto(String name, Long value, Long count, Long min, Long max){
			this.name = name;
			this.value = value;
			this.count = count;
			this.min = min;
			this.max = max;
		}

		private static SingleCountBinaryDto createFromCountStats(Entry<String,CountCollectorStats> entry){
			Require.notNull(entry.getValue());
			var name = Require.notNull(entry.getKey());
			var value = Require.greaterThan(Require.notNull(entry.getValue().sum()), 0L, name);
			var count = Require.greaterThan(Require.notNull(entry.getValue().count()), 0L, name);
			var min = Require.greaterThan(Require.notNull(entry.getValue().min()), 0L, name);
			var max = Require.greaterThan(Require.notNull(entry.getValue().max()), 0L, name);
			return new SingleCountBinaryDto(name, value, count, min, max);
		}

	}

	public static CountBinaryDto decode(byte[] bytes){
		return BinaryDtoIndexedCodec.of(CountBinaryDto.class).decode(bytes);
	}

}
