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

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.bytes.HexBlockTool;
import io.datarouter.metric.counter.CountBinaryDto.SingleCountBinaryDto;
import io.datarouter.metric.service.AggregatedGaugesPublisher.MetricCollectorStats;
import io.datarouter.scanner.Scanner;
import io.datarouter.types.Ulid;

public class CountBinaryDtoTests{

	private static final String
			NAME = "name",
			ULID = new Ulid().value(),
			SERVICE = "service",
			SERVER = "server";
	private static final long PERIOD = Instant.now().toEpochMilli();

	private static final CountBinaryDto OK_DTO = makeDto(SERVICE, SERVER, NAME, ULID, new MetricCollectorStats(1L, 1L,
			1L, 1L), PERIOD);
	private static final BinaryDtoIndexedCodec<CountBinaryDto> CODEC = BinaryDtoIndexedCodec.of(CountBinaryDto.class);

	@Test
	public void testParse20230201(){
		var expected = new CountBinaryDto(
				"01GRCXSMZBFNW5DTTWFFB9SJ3X",
				"myService",
				"myServer",
				1675471661756L,
				List.of(new SingleCountBinaryDto(NAME, 1L)));
//		HexBlockTool.print(expected.encodeIndexed());
		String hex = """
				001a303147524358534d5a42464e57354454545746464239534a335801096d795365727669636502
				086d7953657276657203088000018619e4aabc043101012e00046e616d6501088000000000000001
				020880000000000000020308800000000000000304088000000000000004
				""";
		var actual = CountBinaryDto.decode(HexBlockTool.fromHexBlock(hex));
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testConstructorValidation(){
		var dto = makeDto(SERVICE, SERVER, NAME, ULID, new MetricCollectorStats(1L, 1L, 1L, 1L), PERIOD);
		verifyOkDto(dto);

		Assert.assertThrows(
				IllegalArgumentException.class,
				() -> makeDto("", SERVER, NAME, ULID, new MetricCollectorStats(1L, 1L, 1L, 1L), PERIOD));
		Assert.assertThrows(
				IllegalArgumentException.class,
				() -> makeDto(null, SERVER, NAME, ULID, new MetricCollectorStats(1L, 1L, 1L, 1L), PERIOD));

		Assert.assertThrows(
				IllegalArgumentException.class,
				() -> makeDto(SERVICE, "", NAME, ULID, new MetricCollectorStats(1L, 1L, 1L, 1L), PERIOD));
		Assert.assertThrows(
				IllegalArgumentException.class,
				() -> makeDto(SERVICE, null, NAME, ULID, new MetricCollectorStats(1L, 1L, 1L, 1L), PERIOD));

		Assert.assertThrows(
				IllegalArgumentException.class,
				() -> makeDto(SERVICE, SERVER, null, ULID, new MetricCollectorStats(1L, 1L, 1L, 1L), PERIOD));

		Assert.assertThrows(
				IllegalArgumentException.class,
				() -> makeDto(SERVICE, SERVER, NAME, "", new MetricCollectorStats(1L, 1L, 1L, 1L), PERIOD));
		Assert.assertThrows(
				IllegalArgumentException.class,
				() -> makeDto(SERVICE, SERVER, NAME, null, new MetricCollectorStats(1L, 1L, 1L, 1L), PERIOD));

		Assert.assertThrows(
				IllegalArgumentException.class,
				() -> makeDto(SERVICE, SERVER, NAME, ULID, null, PERIOD));
		Assert.assertThrows(
				IllegalArgumentException.class,
				() -> makeDto(SERVICE, SERVER, NAME, ULID, new MetricCollectorStats(0L, 0L, 0L, 0L), PERIOD));

		Assert.assertThrows(
				IllegalArgumentException.class,
				() -> makeDto(SERVICE, SERVER, NAME, ULID, new MetricCollectorStats(1L, 1L, 1L, 1L), null));
	}

	@Test
	public void testSingleRoundTrip(){
		var dto = makeDto(SERVICE, SERVER, NAME, ULID, new MetricCollectorStats(1L, 1L, 1L, 1L), PERIOD);
		verifyOkDto(CODEC.decode(CODEC.encode(dto)));
	}

	@Test
	public void testCreateSizeDtosAll(){
		int numCounts = 50;
		Map<String,MetricCollectorStats> countValues = new HashMap<>();
		for(int i = 0; i < numCounts; i++){
			countValues.put("counter " + i, new MetricCollectorStats(1L, 1L, 1L, 1L));
		}
		Map<Long,Map<String,MetricCollectorStats>> counts = new HashMap<>();
		counts.put(PERIOD, countValues);

		//test without breaking up
		Assert.assertEquals(
				CountBinaryDto.createSizedCountBinaryDtos(ULID, SERVICE, SERVER, counts, numCounts).get(0).counts
						.size(), numCounts);
		Assert.assertEquals(
				CountBinaryDto.createSizedCountBinaryDtos(ULID, SERVICE, SERVER, counts, countValues.size()).get(
						0).counts.size(), numCounts);

		//test breaking up periods
		Assert.assertEquals(
				CountBinaryDto.createSizedCountBinaryDtos(ULID, SERVICE, SERVER, counts, 1).size(),
				numCounts);

		//test multiple periods
		Map<Long,Map<String,MetricCollectorStats>> multiPeriodCounts = new HashMap<>();
		multiPeriodCounts.put(PERIOD, countValues);
		multiPeriodCounts.put(PERIOD + 1, countValues);
		var multiPeriods = CountBinaryDto.createSizedCountBinaryDtos(ULID, SERVICE, SERVER, multiPeriodCounts,
				numCounts);
		Assert.assertEquals(multiPeriods.size(), 2);
		Assert.assertEquals(multiPeriods.get(0).counts.size(), numCounts);
		Assert.assertEquals(multiPeriods.get(1).counts.size(), numCounts);

		//test that passing Integer.MAX_VALUE as batch size chooses the largest map size from counts.values()
		Assert.assertEquals(CountBinaryDto.createSizedCountBinaryDtos(ULID, SERVICE, SERVER, counts, Integer.MAX_VALUE)
				.get(0).counts.size(), numCounts);

		var copy = Map.copyOf(countValues);
		countValues.put("another", new MetricCollectorStats(5L, 2L, 1L, 4L));
		counts.put(PERIOD + 1, copy);
		Assert.assertEquals(counts.get(PERIOD).size(), numCounts + 1);
		Assert.assertEquals(counts.get(PERIOD + 1).size(), numCounts);
		var dtos = CountBinaryDto.createSizedCountBinaryDtos(ULID, SERVICE, SERVER, counts, Integer.MAX_VALUE);
		Assert.assertEquals(
				Scanner.of(dtos)
						.map(dto -> dto.counts.size())
						.collect(HashSet::new),
				Set.of(numCounts, numCounts + 1));
	}

	private void verifyOkDto(CountBinaryDto dto){
		Assert.assertEquals(dto.period, PERIOD);
		Assert.assertEquals(dto.serviceName, OK_DTO.serviceName);
		Assert.assertEquals(dto.serverName, OK_DTO.serverName);
		Assert.assertEquals(dto.counts.get(0).name, OK_DTO.counts.get(0).name);
		Assert.assertEquals(dto.ulid, OK_DTO.ulid);
		Assert.assertEquals(dto.counts.get(0).value, OK_DTO.counts.get(0).value);
	}

	private static CountBinaryDto makeDto(
			String service,
			String server,
			String name,
			String ulid,
			MetricCollectorStats value,
			Long period){
		Map<String,MetricCollectorStats> countValues = new HashMap<>();
		countValues.put(name, value);
		Map<Long,Map<String,MetricCollectorStats>> counts = new HashMap<>();
		counts.put(period, countValues);
		return CountBinaryDto.createSizedCountBinaryDtos(ulid, service, server, counts, 1).get(0);
	}

}
