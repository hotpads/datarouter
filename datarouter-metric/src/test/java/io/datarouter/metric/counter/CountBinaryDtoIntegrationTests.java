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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.Ulid;

public class CountBinaryDtoIntegrationTests{

	private static final String
			NAME = "name",
			ULID = new Ulid().value(),
			SERVICE = "service",
			SERVER = "server";
	private static final long PERIOD = Instant.now().toEpochMilli();

	private static final CountBinaryDto OK_DTO = makeDto(SERVICE, SERVER, NAME, ULID, 1L, PERIOD);
	private static final BinaryDtoIndexedCodec<CountBinaryDto> CODEC = BinaryDtoIndexedCodec.of(CountBinaryDto.class);

	@Test
	public void testConstructorValidation(){
		var dto = makeDto(SERVICE, SERVER, NAME, ULID, 1L, PERIOD);
		verifyOkDto(dto);

		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeDto("", SERVER, NAME, ULID, 1L, PERIOD));
		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeDto(null, SERVER, NAME, ULID, 1L, PERIOD));

		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeDto(SERVICE, "", NAME, ULID, 1L, PERIOD));
		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeDto(SERVICE, null, NAME, ULID, 1L, PERIOD));

		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeDto(SERVICE, SERVER, null, ULID, 1L, PERIOD));

		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeDto(SERVICE, SERVER, NAME, "", 1L, PERIOD));
		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeDto(SERVICE, SERVER, NAME, null, 1L, PERIOD));

		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeDto(SERVICE, SERVER, NAME, ULID, null, PERIOD));
		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeDto(SERVICE, SERVER, NAME, ULID, 0L, PERIOD));

		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeDto(SERVICE, SERVER, NAME, ULID, 1L, null));
	}

	@Test
	public void testSingleRoundTrip(){
		var dto = makeDto(SERVICE, SERVER, NAME, ULID, 1L, PERIOD);
		verifyOkDto(CODEC.decode(CODEC.encode(dto)));
	}

	@Test
	public void testCreateSizeDtosAll(){
		Map<String,Long> countValues = new HashMap<>();
		for(int i = 0; i < 50; i++){
			countValues.put("counter " + i, 1L);
		}
		Map<Long,Map<String,Long>> counts = new HashMap<>();
		counts.put(PERIOD, countValues);

		//test without breaking up
		Assert.assertEquals(CountBinaryDto.createSizedDtos(ULID, SERVICE, SERVER, counts, 50).get(0).counts.size(), 50);
		Assert.assertEquals(
				CountBinaryDto.createSizedDtos(ULID, SERVICE, SERVER, counts, countValues.size()).get(0).counts.size(),
				50);

		//test breaking up periods
		Assert.assertEquals(
				CountBinaryDto.createSizedDtos(ULID, SERVICE, SERVER, counts, 1).size(),
				50);

		//test multiple periods
		Map<Long,Map<String,Long>> multiPeriodCounts = new HashMap<>();
		multiPeriodCounts.put(PERIOD, countValues);
		multiPeriodCounts.put(PERIOD + 1, countValues);
		var multiPeriods = CountBinaryDto.createSizedDtos(ULID, SERVICE, SERVER, multiPeriodCounts, 50);
		Assert.assertEquals(multiPeriods.size(), 2);
		Assert.assertEquals(multiPeriods.get(0).counts.size(), 50);
		Assert.assertEquals(multiPeriods.get(1).counts.size(), 50);

		//test that passing Integer.MAX_VALUE as batch size chooses the largest map size from counts.values()
		Assert.assertEquals(
				CountBinaryDto.createSizedDtos(ULID, SERVICE, SERVER, counts, Integer.MAX_VALUE).get(0).counts.size(),
				50);

		var copy = Map.copyOf(countValues);
		countValues.put("another", 5L);
		counts.put(PERIOD + 1, copy);
		Assert.assertEquals(counts.get(PERIOD).size(), 51);
		Assert.assertEquals(counts.get(PERIOD + 1).size(), 50);
		var dtos = CountBinaryDto.createSizedDtos(ULID, SERVICE, SERVER, counts, Integer.MAX_VALUE);
		Assert.assertEquals(
				Scanner.of(dtos)
						.map(dto -> dto.counts.size())
						.collect(Collectors.toSet()),
				Set.of(50, 51));
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
			String service, String server, String name, String ulid, Long value, Long period){
		Map<String,Long> countValues = new HashMap<>();
		countValues.put(name, value);
		Map<Long,Map<String,Long>> counts = new HashMap<>();
		counts.put(period, countValues);
		return CountBinaryDto.createSizedDtos(ulid, service, server, counts, 1).get(0);
	}

}
