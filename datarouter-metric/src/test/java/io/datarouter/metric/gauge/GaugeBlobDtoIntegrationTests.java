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

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.instrumentation.gauge.GaugeBatchDto;
import io.datarouter.instrumentation.gauge.GaugeDto;
import io.datarouter.metric.dto.GaugeBlobDto;
import io.datarouter.metric.dto.GaugeBlobDto.GaugeBlobItemDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.UlidTool;

public class GaugeBlobDtoIntegrationTests{

	private static final List<GaugeBlobItemDto> nonEmptyItems = List.of(
			new GaugeBlobItemDto("gauge", UlidTool.nextUlid(), 1L));
	private static final String
			ulid = UlidTool.nextUlid(),
			service = "service",
			server = "server",
			apiKey = "apiKey";

	@Test
	public void testConstructorValidation(){
		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeBlob("", server, nonEmptyItems, apiKey));
		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeBlob(null, server, nonEmptyItems, apiKey));

		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeBlob(service, "", nonEmptyItems, apiKey));
		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeBlob(service, null, nonEmptyItems, apiKey));


		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeBlob(service, server, List.of(), apiKey));
		//makeBlob can't reproduce this situation by passing null for items
		GaugeBatchDto dto = null;
		Assert.assertThrows(NullPointerException.class, () ->
				new GaugeBlobDto(dto, apiKey));

		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeBlob(service, server, nonEmptyItems, ""));
		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeBlob(service, server, nonEmptyItems, null));
	}

	@Test
	public void testConstructorFields(){
		var okDto = makeBlob(service, server, nonEmptyItems, apiKey);
		verifyOkDto(okDto);
	}

	@Test
	public void testRoundTripBytes(){
		var okDto = makeBlob(service, server, nonEmptyItems, apiKey);
		var bytes = okDto.serializeToBytes();
		verifyOkDto(GaugeBlobDto.deserializeFromString(new String(bytes, StandardCharsets.UTF_8)));
	}

	@Test
	public void testRoundTripSingleString(){
		var okDto = makeBlob(service, server, nonEmptyItems, apiKey);
		List<String> strings = okDto.serializeToStrings(Integer.MAX_VALUE).list();
		Assert.assertEquals(strings.size(), 1);
		verifyOkDto(GaugeBlobDto.deserializeFromString(strings.get(0)));
	}

	@Test
	public void testRoundTripSplitString(){
		int ulidLength = ulid.length();
		String gaugeName = "first really long gauge name";
		String gaugeName2 = "second really long gauge name";
		List<GaugeBlobItemDto> items = List.of(
				new GaugeBlobItemDto(gaugeName, UlidTool.nextUlid(), 1L),
				new GaugeBlobItemDto(gaugeName2, UlidTool.nextUlid(), 1L));

		var okDto = makeBlob(service, server, items, apiKey);

		int metadataLineLength = (String.join("\t", List.of(okDto.version,
				okDto.serviceName,
				okDto.serverName,
				okDto.apiKey)) + "\n").length();
		int sizeToCauseSplit = metadataLineLength + gaugeName2.length() + ulidLength + 3;

		List<String> strings = okDto.serializeToStrings(sizeToCauseSplit).list();
		Assert.assertEquals(strings.size(), 2);
		verifyOkDto(
				Scanner.of(strings)
						.map(GaugeBlobDto::deserializeFromString)
						.list(),
				items);
	}

	private void verifyOkDto(GaugeBlobDto okDto){
		verifyOkDtoNonItemFields(okDto);
		verifyItems(okDto.items, nonEmptyItems);
	}

	private void verifyOkDto(List<GaugeBlobDto> dtos, List<GaugeBlobItemDto> unsplitItems){
		List<GaugeBlobItemDto> combinedItems = Scanner.of(dtos)
				.each(this::verifyOkDtoNonItemFields)
				.concatIter(dto -> dto.items)
				.list();
		verifyItems(combinedItems, unsplitItems);
	}

	private void verifyItems(List<GaugeBlobItemDto> actualItems, List<GaugeBlobItemDto> expectedItems){
		Assert.assertEquals(actualItems.size(), expectedItems.size());
		for(int i = 0; i < expectedItems.size(); i++){
			var actual = actualItems.get(i);
			var expected = expectedItems.get(i);
			Assert.assertEquals(actual.name, expected.name);
			Assert.assertEquals(actual.ulid, expected.ulid);
			Assert.assertEquals(actual.value, expected.value);
		}
	}

	private void verifyOkDtoNonItemFields(GaugeBlobDto okDto){
		Assert.assertEquals(okDto.version, "v1");
		Assert.assertEquals(okDto.serviceName, service);
		Assert.assertEquals(okDto.serverName, server);
		Assert.assertEquals(okDto.apiKey, apiKey);
	}

	private GaugeBlobDto makeBlob(String service, String server, List<GaugeBlobItemDto> items, String apikey){
		var batchDto = Scanner.of(items)
				.map(item -> new GaugeDto(item.name, service, server, item.ulid, item.value))
				.listTo(GaugeBatchDto::new);
		return new GaugeBlobDto(batchDto, apikey);
	}

}
