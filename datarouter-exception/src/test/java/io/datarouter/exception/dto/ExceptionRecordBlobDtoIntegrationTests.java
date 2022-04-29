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

import io.datarouter.exception.dto.ExceptionRecordBlobDto.ExceptionRecordBlobItemDto;
import io.datarouter.gson.serialization.GsonTool;
import io.datarouter.instrumentation.exception.ExceptionRecordBatchDto;
import io.datarouter.instrumentation.exception.ExceptionRecordDto;
import io.datarouter.scanner.Scanner;

public class ExceptionRecordBlobDtoIntegrationTests{

	private static final String
			id = "id",
			service = "service",
			server = "server",
			apiKey = "apiKey",
			appVersion = "appVersion";

	private static final ExceptionRecordDto VALID_DTO = new ExceptionRecordDto(
			id,
			null,
			service,
			server,
			null,
			null,
			null,
			null,
			appVersion,
			null,
			null,
			null,
			null);

	@Test
	public void testConstructorValidation(){
		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeBlob(id, "", server, appVersion, apiKey));
		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeBlob(id, null, server, appVersion, apiKey));

		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeBlob(id, service, "", appVersion, apiKey));
		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeBlob(id, service, null, appVersion, apiKey));

		ExceptionRecordBatchDto emptyDto = new ExceptionRecordBatchDto(List.of());
		Assert.assertThrows(IllegalArgumentException.class, () ->
				new ExceptionRecordBlobDto(emptyDto, apiKey));
		Assert.assertThrows(NullPointerException.class, () ->
				new ExceptionRecordBlobDto(null, apiKey));

		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeBlob(id, service, server, appVersion, ""));
		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeBlob(id, service, server, appVersion, null));
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
		verifyOkDto(ExceptionRecordBlobDto.deserializeFromString(new String(bytes, StandardCharsets.UTF_8)));
	}

	@Test
	public void testRoundTripSingleString(){
		var okDto = makeBlob(List.of(VALID_DTO), apiKey);
		List<String> strings = okDto.serializeToStrings(Integer.MAX_VALUE).list();
		Assert.assertEquals(strings.size(), 1);
		verifyOkDto(ExceptionRecordBlobDto.deserializeFromString(strings.get(0)));
	}

	@Test
	public void testRoundTripSplitString(){
		var validDto2 = new ExceptionRecordDto(
				id + "2",
				null,
				service,
				server,
				null,
				null,
				null,
				null,
				appVersion,
				null,
				null,
				null,
				null);
		var items = List.of(VALID_DTO, validDto2);
		var okDto = makeBlob(items, apiKey);

		int metadataLineLength = (String.join("\t", List.of(okDto.version,
				okDto.serviceName,
				okDto.serverName,
				okDto.appVersion,
				okDto.apiKey)) + "\n").length();
		int sizeToCauseSplit = metadataLineLength + 1 + GsonTool.JAVA9_GSON.toJson(okDto.items.get(0)).length();

		List<String> strings = okDto.serializeToStrings(sizeToCauseSplit).list();
		Assert.assertEquals(strings.size(), 2);
		verifyOkDto(
				Scanner.of(strings)
						.map(ExceptionRecordBlobDto::deserializeFromString)
						.list(),
				items);
	}

	private void verifyOkDto(ExceptionRecordBlobDto okDto){
		verifyOkDtoNonItemFields(okDto);
		verifyItems(okDto.items, List.of(VALID_DTO));
	}

	private void verifyOkDto(List<ExceptionRecordBlobDto> dtos, List<ExceptionRecordDto> unsplitItems){
		List<ExceptionRecordBlobItemDto> combinedItems = Scanner.of(dtos)
				.each(this::verifyOkDtoNonItemFields)
				.concatIter(dto -> dto.items)
				.list();
		verifyItems(combinedItems, unsplitItems);
	}

	private void verifyItems(List<ExceptionRecordBlobItemDto> actualItems,
			List<ExceptionRecordDto> expectedItems){
		Assert.assertEquals(actualItems.size(), expectedItems.size());
		for(int i = 0; i < expectedItems.size(); i++){
			var actual = actualItems.get(i);
			var expected = expectedItems.get(i);
			Assert.assertEquals(actual.id, expected.id);
			Assert.assertEquals(actual.created, expected.created);
			Assert.assertEquals(actual.category, expected.category);
			Assert.assertEquals(actual.name, expected.name);
			Assert.assertEquals(actual.stackTrace, expected.stackTrace);
			Assert.assertEquals(actual.type, expected.type);
			Assert.assertEquals(actual.exceptionLocation, expected.exceptionLocation);
			Assert.assertEquals(actual.methodName, expected.methodName);
			Assert.assertEquals(actual.lineNumber, expected.lineNumber);
			Assert.assertEquals(actual.callOrigin, expected.callOrigin);
		}
	}

	private void verifyOkDtoNonItemFields(ExceptionRecordBlobDto okDto){
		Assert.assertEquals(okDto.version, "v1");
		Assert.assertEquals(okDto.serviceName, service);
		Assert.assertEquals(okDto.serverName, server);
		Assert.assertEquals(okDto.appVersion, appVersion);
		Assert.assertEquals(okDto.apiKey, apiKey);
	}

	private ExceptionRecordBlobDto makeBlob(String id, String service, String server, String appVersion, String apiKey){
		var dto = new ExceptionRecordDto(
				id,
				null,
				service,
				server,
				null,
				null,
				null,
				null,
				appVersion,
				null,
				null,
				null,
				null);
		return makeBlob(List.of(dto), apiKey);
	}

	private ExceptionRecordBlobDto makeBlob(List<ExceptionRecordDto> dtos, String apiKey){
		return new ExceptionRecordBlobDto(new ExceptionRecordBatchDto(dtos), apiKey);
	}

}
