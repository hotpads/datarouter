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

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.UlidTool;

public class CountBlobDtoIntegrationTests{

	private static final Map<Long,Map<String,Long>> nonEmptyMap = Map.of(1L, Map.of("counter", 1L));
	private static final String
			ulid = UlidTool.nextUlid(),
			service = "service",
			server = "server",
			apiKey = "apiKey";

	@Test
	public void testConstructorValidation(){
		Assert.assertThrows(IllegalArgumentException.class, () ->
				new CountBlobDto("", service, server, nonEmptyMap, apiKey));
		Assert.assertThrows(IllegalArgumentException.class, () ->
				new CountBlobDto(null, service, server, nonEmptyMap, apiKey));

		Assert.assertThrows(IllegalArgumentException.class, () ->
				new CountBlobDto(UlidTool.nextUlid(), "", server, nonEmptyMap, apiKey));
		Assert.assertThrows(IllegalArgumentException.class, () ->
				new CountBlobDto(UlidTool.nextUlid(), null, server, nonEmptyMap, apiKey));

		Assert.assertThrows(IllegalArgumentException.class, () ->
				new CountBlobDto(UlidTool.nextUlid(), service, "", nonEmptyMap, apiKey));
		Assert.assertThrows(IllegalArgumentException.class, () ->
				new CountBlobDto(UlidTool.nextUlid(), service, null, nonEmptyMap, apiKey));

		Assert.assertThrows(IllegalArgumentException.class, () ->
				new CountBlobDto(UlidTool.nextUlid(), service, server, Map.of(), apiKey));
		Assert.assertThrows(NullPointerException.class, () ->
				new CountBlobDto(UlidTool.nextUlid(), service, server, null, apiKey));


		Assert.assertThrows(IllegalArgumentException.class, () ->
				new CountBlobDto(UlidTool.nextUlid(), service, server, nonEmptyMap, ""));
		Assert.assertThrows(IllegalArgumentException.class, () ->
				new CountBlobDto(UlidTool.nextUlid(), service, server, nonEmptyMap, null));
	}

	@Test
	public void testConstructorFields(){
		var okDto = new CountBlobDto(ulid, service, server, nonEmptyMap, apiKey);
		verifyOkDto(okDto);
	}

	@Test
	public void testRoundTripBytes(){
		var okDto = new CountBlobDto(ulid, service, server, nonEmptyMap, apiKey);
		var bytes = okDto.serializeToBytes();
		verifyOkDto(CountBlobDto.deserializeFromString(new String(bytes, StandardCharsets.UTF_8)));
	}

	@Test
	public void testRoundTripSingleString(){
		var okDto = new CountBlobDto(ulid, service, server, nonEmptyMap, apiKey);
		List<String> strings = okDto.serializeToStrings(Integer.MAX_VALUE).list();
		Assert.assertEquals(strings.size(), 1);
		verifyOkDto(CountBlobDto.deserializeFromString(strings.get(0)));
	}

	@Test
	public void testRoundTripSplitString(){
		long timestamp = UlidTool.getTimestampMs(ulid);
		int timestampLength = String.valueOf(timestamp).length();
		String counterName = "first really long counter name";
		String counterName2 = "second really long counter name";
		var unsplitMap = Map.of(timestamp, Map.of(counterName, 1L, counterName2, 1L));

		var okDto = new CountBlobDto(ulid, service, server, unsplitMap, apiKey);

		int metadataLineLength = (String.join("\t", List.of(okDto.version,
				okDto.ulid,
				okDto.serviceName,
				okDto.serverName,
				okDto.apiKey,
				okDto.signature)) + "\n").length();
		int sizeToCauseSplit = metadataLineLength + timestampLength + counterName2.length() + 3;

		List<String> strings = okDto.serializeToStrings(sizeToCauseSplit).list();
		Assert.assertEquals(strings.size(), 2);
		verifyOkDto(
				Scanner.of(strings)
						.map(CountBlobDto::deserializeFromString)
						.list(),
				unsplitMap);
	}

	private void verifyOkDto(CountBlobDto okDto){
		verifyOkDtoNonMapFields(okDto);
		Assert.assertEquals(okDto.counts, nonEmptyMap);
	}

	private void verifyOkDto(List<CountBlobDto> dtos, Map<Long,Map<String,Long>> unsplitMap){
		Map<Long,Map<String,Long>> combinedMap = Scanner.of(dtos)
				.each(this::verifyOkDtoNonMapFields)
				.concatIter(dto -> dto.counts.entrySet())
				.toMap(Entry::getKey, Entry::getValue, (countMap1, countMap2) -> {
					var combined = new HashMap<>(countMap1);
					countMap2.entrySet().forEach(entry -> combined.merge(entry.getKey(), entry.getValue(), Long::sum));
					return combined;
				});
		Assert.assertEquals(combinedMap, unsplitMap);
	}

	private void verifyOkDtoNonMapFields(CountBlobDto okDto){
		Assert.assertEquals(okDto.version, "v1");
		Assert.assertEquals(okDto.ulid, ulid);
		Assert.assertEquals(okDto.serviceName, service);
		Assert.assertEquals(okDto.serverName, server);
		Assert.assertEquals(okDto.apiKey, apiKey);
		Assert.assertEquals(okDto.signature, "unused");
	}

}
