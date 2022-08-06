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
import java.util.Base64;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.exception.dto.ExceptionRecordBlobDto.ExceptionRecordBlobItemDto;
import io.datarouter.gson.serialization.GsonTool;
import io.datarouter.instrumentation.exception.ExceptionRecordBatchDto;
import io.datarouter.instrumentation.exception.ExceptionRecordDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.queue.StringQueueMessage;
import io.datarouter.util.UlidTool;

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

	private static final int EXTRA_JSON_LENGTH = "{\"MessageId\":\"\",\"Message\":\"\"}".length();

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
	public void testRoundTripSplitStringWithV1Serialization(){
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

		int metadataLineLength = (String.join("\t", List.of(
				"v1",
				okDto.serviceName,
				okDto.serverName,
				okDto.appVersion,
				okDto.apiKey)) + "\n").length();
		int sizeToCauseSplit = metadataLineLength + 1 + GsonTool.GSON.toJson(okDto.items.get(0)).length();

		List<String> strings = okDto.serializeToStringsOld(sizeToCauseSplit).list();
		Assert.assertEquals(strings.size(), 2);
		verifyOkDto(
				Scanner.of(strings)
						.map(ExceptionRecordBlobDto::deserializeFromString)
						.list(),
				items);
	}

	@Test
	public void testTotalDatabeanSizePrediction(){
		var fielder = new StringQueueMessage.UnlimitedSizeStringQueueMessageFielder();
		String ulid = UlidTool.nextUlid();
		Assert.assertEquals(
				fielder.getStringDatabeanCodec().toString(new StringQueueMessage(ulid, ""), fielder).length(),
				ulid.length() + EXTRA_JSON_LENGTH);

		//build 2 encoded items
		var builder = new ExceptionRecordBlobDto.ExceptionRecordsSplittingStringBuildersV2(
				ExceptionRecordsSplittingStringBuildersV2IntegrationTests.EMPTY_LENGTH * 2 + 1);
		builder.append(ExceptionRecordsSplittingStringBuildersV2IntegrationTests.EMPTY);
		builder.append(ExceptionRecordsSplittingStringBuildersV2IntegrationTests.EMPTY);
		var list = builder.scanSplitItems().list();
		Assert.assertEquals(list.size(), 1);

		int expectedLength =
				ulid.length()
				+ EXTRA_JSON_LENGTH
				+ ExceptionRecordsSplittingStringBuildersV2IntegrationTests.EMPTY_LENGTH * 2//2 encoded items
				+ 1;//1 comma to separate 2 items
		Assert.assertEquals(
				fielder.getStringDatabeanCodec().toString(new StringQueueMessage(ulid, list.get(0)), fielder).length(),
				expectedLength);
	}

	@Test
	public void testRoundTripSplitStringWithV2Serialization(){
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

		int metadataLineLength = (toBase64ByteString(String.join("\t", List.of(
				"v2",
				okDto.serviceName,
				okDto.serverName,
				okDto.appVersion,
				okDto.apiKey))) + ",").length();
		int sizeToCauseSplit = metadataLineLength
				+ toBase64ByteString(GsonTool.GSON.toJson(okDto.items.get(0))).length();

		List<String> strings = okDto.serializeToStrings(sizeToCauseSplit).list();
		Assert.assertEquals(strings.size(), 2);
		verifyOkDto(
				Scanner.of(strings)
						.map(ExceptionRecordBlobDto::deserializeFromString)
						.list(),
				items);
	}

	private static String toBase64ByteString(String string){
		return Base64.getEncoder().encodeToString(string.getBytes(StandardCharsets.UTF_8));
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
		Assert.assertEquals(okDto.version, "v2");
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
