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
package io.datarouter.trace.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.conveyor.message.ConveyorMessage;
import io.datarouter.gson.serialization.GsonTool;
import io.datarouter.instrumentation.trace.Trace2BundleDto;
import io.datarouter.instrumentation.trace.Trace2Dto;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.UlidTool;

public class TraceBlobDtoIntegrationTests{

	private static final String apiKey = "apiKey";

	private static final Trace2BundleDto VALID_BUNDLE_DTO = makeBundleDtoBlob("id1");

	private static final int EXTRA_JSON_LENGTH = "{\"MessageId\":\"\",\"Message\":\"\"}".length();

	@Test
	public void testConstructorValidation(){
		Assert.assertThrows(IllegalArgumentException.class, () -> new TraceBlobDto("", List.of(VALID_BUNDLE_DTO)));
		Assert.assertThrows(IllegalArgumentException.class, () -> new TraceBlobDto(null, List.of(VALID_BUNDLE_DTO)));

		Assert.assertThrows(IllegalArgumentException.class, () -> new TraceBlobDto(apiKey, List.of()));
		Assert.assertThrows(IllegalArgumentException.class, () -> new TraceBlobDto(apiKey, null));
	}

	@Test
	public void testConstructorFields(){
		var okDto = makeBlob(List.of(VALID_BUNDLE_DTO), apiKey);
		verifyOkDto(okDto);
	}

	@Test
	public void testRoundTripBytes(){
		var okDto = makeBlob(List.of(VALID_BUNDLE_DTO), apiKey);
		var bytes = okDto.serializeToBytes();
		verifyOkDto(TraceBlobDto.deserializeFromString(new String(bytes, StandardCharsets.UTF_8)));
	}

	@Test
	public void testRoundTripSingleString(){
		var okDto = makeBlob(List.of(VALID_BUNDLE_DTO), apiKey);
		List<String> strings = okDto.serializeToStrings(Integer.MAX_VALUE).list();
		Assert.assertEquals(strings.size(), 1);
		verifyOkDto(TraceBlobDto.deserializeFromString(strings.get(0)));
	}

	@Test
	public void testTotalDatabeanSizePrediction(){
		var fielder = new ConveyorMessage.UnlimitedSizeConveyorMessageFielder();
		String ulid = UlidTool.nextUlid();
		Assert.assertEquals(
				fielder.getStringDatabeanCodec().toString(new ConveyorMessage(ulid, ""), fielder).length(),
				ulid.length() + EXTRA_JSON_LENGTH);

		//build 2 encoded items
		var builder = new TraceBlobDto.TracesSplittingStringBuilders(
				TracesSplittingStringBuildersIntegrationTests.EMPTY_LENGTH * 2 + 1);
		builder.append(TracesSplittingStringBuildersIntegrationTests.EMPTY);
		builder.append(TracesSplittingStringBuildersIntegrationTests.EMPTY);
		var list = builder.scanSplitItems().list();
		Assert.assertEquals(list.size(), 1);

		int expectedLength =
				ulid.length()
				+ EXTRA_JSON_LENGTH
				+ TracesSplittingStringBuildersIntegrationTests.EMPTY_LENGTH * 2//2 encoded items
				+ 1;//1 comma to separate 2 items
		Assert.assertEquals(
				fielder.getStringDatabeanCodec().toString(new ConveyorMessage(ulid, list.get(0)), fielder).length(),
				expectedLength);
	}

	@Test
	public void testRoundTripSplitStringWithSerialization(){
		var validDto2 = makeBundleDtoBlob("id2");
		var items = List.of(VALID_BUNDLE_DTO, validDto2);
		var okDto = makeBlob(items, apiKey);

		int metadataLineLength = (toBase64ByteString(String.join("\t", List.of("v1", apiKey))) + ",").length();
		int sizeToCauseSplit = metadataLineLength
				+ toBase64ByteString(GsonTool.GSON.toJson(okDto.traceBundles.get(0))).length();

		List<String> strings = okDto.serializeToStrings(sizeToCauseSplit).list();
		Assert.assertEquals(strings.size(), 2);
		verifyOkDto(
				Scanner.of(strings)
						.map(TraceBlobDto::deserializeFromString)
						.list(),
				items);
	}

	private static String toBase64ByteString(String string){
		return Base64.getEncoder().encodeToString(string.getBytes(StandardCharsets.UTF_8));
	}

	private void verifyOkDto(TraceBlobDto okDto){
		verifyOkDtoNonItemFields(okDto);
		verifyItems(okDto.traceBundles, List.of(VALID_BUNDLE_DTO));
	}

	private void verifyOkDto(List<TraceBlobDto> dtos, List<Trace2BundleDto> unsplitDtos){
		List<Trace2BundleDto> recombinedDtos = Scanner.of(dtos)
				.each(this::verifyOkDtoNonItemFields)
				.concatIter(dto -> dto.traceBundles)
				.list();
		verifyItems(recombinedDtos, unsplitDtos);
	}

	private void verifyItems(List<Trace2BundleDto> actualDtos,
			List<Trace2BundleDto> expectedDtos){
		Assert.assertEquals(actualDtos.size(), expectedDtos.size());
		for(int i = 0; i < expectedDtos.size(); i++){
			var actual = actualDtos.get(i);
			var expected = expectedDtos.get(i);
			if(actual.traceDto != null && expected.traceDto != null){
				Assert.assertEquals(actual.traceDto.initialParentId, expected.traceDto.initialParentId);
			}else{
				Assert.assertEquals(actual.traceDto, expected.traceDto);
			}
			Assert.assertEquals(actual.traceThreadDtos, expected.traceThreadDtos);
			Assert.assertEquals(actual.traceSpanDtos, expected.traceSpanDtos);
		}
	}

	private void verifyOkDtoNonItemFields(TraceBlobDto okDto){
		Assert.assertEquals(okDto.apiKey, apiKey);
	}

	private static Trace2BundleDto makeBundleDtoBlob(String id){
		Trace2Dto dto = new Trace2Dto(null, id, null, null, null, null, null, null, null, null, null, null, null, null,
				null);
		return new Trace2BundleDto(dto, null, null);
	}

	private static TraceBlobDto makeBlob(List<Trace2BundleDto> dtos, String apiKey){
		return new TraceBlobDto(apiKey, dtos);
	}

}
