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
package io.datarouter.exception.dto;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.gson.serialization.GsonTool;
import io.datarouter.instrumentation.exception.HttpRequestRecordBatchDto;
import io.datarouter.instrumentation.exception.HttpRequestRecordDto;
import io.datarouter.scanner.Scanner;

public class HttpRequestRecordBlobDtoIntegrationTests{

	private static final String apiKey = "apiKey";

	private static final HttpRequestRecordDto VALID_DTO = makeDto("id");

	@Test
	public void testConstructorValidation(){
		HttpRequestRecordBatchDto emptyDto = new HttpRequestRecordBatchDto(List.of());
		Assert.assertThrows(IllegalArgumentException.class, () -> new HttpRequestRecordBlobDto(emptyDto, apiKey));
		Assert.assertThrows(NullPointerException.class, () -> new HttpRequestRecordBlobDto(null, apiKey));

		Assert.assertThrows(IllegalArgumentException.class, () -> makeBlob(List.of(VALID_DTO), ""));
		Assert.assertThrows(IllegalArgumentException.class, () -> makeBlob(List.of(VALID_DTO), null));
	}

	@Test
	public void testConstructorFields(){
		var okDto = makeBlob(List.of(VALID_DTO), apiKey);
		verifyOkDto(okDto);
	}

	@Test
	public void testRoundTripBytes(){
		var okDto = makeBlob(List.of(VALID_DTO), apiKey);
		var bytes = okDto.serializeToBytes();
		verifyOkDto(HttpRequestRecordBlobDto.deserializeFromString(new String(bytes, StandardCharsets.UTF_8)));
	}

	@Test
	public void testRoundTripSingleString(){
		var okDto = makeBlob(List.of(VALID_DTO), apiKey);
		List<String> strings = okDto.serializeToStrings(Integer.MAX_VALUE).list();
		Assert.assertEquals(strings.size(), 1);
		verifyOkDto(HttpRequestRecordBlobDto.deserializeFromString(strings.get(0)));
	}

	@Test
	public void testRoundTripSplitString(){
		HttpRequestRecordDto validDto2 = makeDto("id2");
		var items = List.of(VALID_DTO, validDto2);
		var okDto = makeBlob(items, apiKey);

		int metadataLineLength = (String.join("\t", List.of(okDto.version, okDto.apiKey)) + "\n").length();
		int sizeToCauseSplit = metadataLineLength + 1 + GsonTool.GSON.toJson(okDto.items.get(0)).length();

		List<String> strings = okDto.serializeToStrings(sizeToCauseSplit).list();
		Assert.assertEquals(strings.size(), 2);
		verifyOkDto(
				Scanner.of(strings)
						.map(HttpRequestRecordBlobDto::deserializeFromString)
						.list(),
				items);
	}

	private void verifyOkDto(HttpRequestRecordBlobDto okDto){
		verifyOkDtoNonItemFields(okDto);
		verifyItems(okDto.items, List.of(VALID_DTO));
	}

	private void verifyOkDto(List<HttpRequestRecordBlobDto> dtos, List<HttpRequestRecordDto> unsplitItems){
		List<HttpRequestRecordDto> combinedItems = Scanner.of(dtos)
				.each(this::verifyOkDtoNonItemFields)
				.concatIter(dto -> dto.items)
				.list();
		verifyItems(combinedItems, unsplitItems);
	}

	private void verifyItems(List<HttpRequestRecordDto> actualItems,
			List<HttpRequestRecordDto> expectedItems){
		Assert.assertEquals(actualItems.size(), expectedItems.size());
		for(int i = 0; i < expectedItems.size(); i++){
			var actual = actualItems.get(i);
			var expected = expectedItems.get(i);
			Assert.assertEquals(actual.id, expected.id);
		}
	}

	private void verifyOkDtoNonItemFields(HttpRequestRecordBlobDto okDto){
		Assert.assertEquals(okDto.version, "v1");
		Assert.assertEquals(okDto.apiKey, apiKey);
	}

	private HttpRequestRecordBlobDto makeBlob(List<HttpRequestRecordDto> dtos, String apiKey){
		return new HttpRequestRecordBlobDto(new HttpRequestRecordBatchDto(dtos), apiKey);
	}

	private static HttpRequestRecordDto makeDto(String id){
		return new HttpRequestRecordDto(id, null, null, null, null, null, null, null, null, null, null, 0, null, null,
				null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
				null, null, null, null, null, null, null, null, null);
	}

}
