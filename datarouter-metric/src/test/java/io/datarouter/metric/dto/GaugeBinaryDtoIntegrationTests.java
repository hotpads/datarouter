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
package io.datarouter.metric.dto;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.instrumentation.gauge.GaugeBatchDto;
import io.datarouter.instrumentation.gauge.GaugeDto;
import io.datarouter.util.UlidTool;

public class GaugeBinaryDtoIntegrationTests{

	private static final String
			NAME = "name",
			ULID = UlidTool.nextUlid(),
			SERVICE = "service",
			SERVER = "server",
			API_KEY = "apiKey";

	private static final GaugeDto OK_DTO = new GaugeDto(NAME, SERVICE, SERVER, ULID, 1L);

	private static final BinaryDtoIndexedCodec<GaugeBinaryDto> CODEC = BinaryDtoIndexedCodec.of(GaugeBinaryDto.class);

	@Test
	public void testConstructorValidation(){
		var dto = makeBlob(SERVICE, SERVER, NAME, ULID, 1L, API_KEY);
		verifyOkDto(dto);

		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeBlob("", SERVER, NAME, ULID, 1L, API_KEY));
		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeBlob(null, SERVER, NAME, ULID, 1L, API_KEY));

		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeBlob(SERVICE, "", NAME, ULID, 1L, API_KEY));
		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeBlob(SERVICE, null, NAME, ULID, 1L, API_KEY));

		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeBlob(SERVICE, SERVER, "", ULID, 1L, API_KEY));
		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeBlob(SERVICE, SERVER, null, ULID, 1L, API_KEY));

		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeBlob(SERVICE, SERVER, NAME, "", 1L, API_KEY));
		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeBlob(SERVICE, SERVER, NAME, null, 1L, API_KEY));

		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeBlob(SERVICE, SERVER, NAME, ULID, null, API_KEY));

		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeBlob(SERVICE, SERVER, NAME, ULID, 1L, ""));
		Assert.assertThrows(IllegalArgumentException.class, () ->
				makeBlob(SERVICE, SERVER, NAME, ULID, 1L, null));

		Assert.assertThrows(IllegalArgumentException.class, () ->
				GaugeBinaryDto.createSizedDtos(null, API_KEY, Integer.MAX_VALUE).get(0));
		var nullBatch = new GaugeBatchDto(null);
		Assert.assertThrows(IllegalArgumentException.class, () ->
				GaugeBinaryDto.createSizedDtos(nullBatch, API_KEY, Integer.MAX_VALUE).get(0));
		var emptyBatch = new GaugeBatchDto(List.of());
		Assert.assertThrows(IllegalArgumentException.class, () ->
				GaugeBinaryDto.createSizedDtos(emptyBatch, API_KEY, Integer.MAX_VALUE).get(0));
	}

	@Test
	public void testSingleRoundTrip(){
		var dto = makeBlob(SERVICE, SERVER, NAME, ULID, 1L, API_KEY);
		verifyOkDto(CODEC.decode(CODEC.encode(dto)));
	}

	private void verifyOkDto(GaugeBinaryDto dto){
		Assert.assertEquals(dto.apiKey, API_KEY);
		var actual = dto.toGaugeDtos().get(0);
		Assert.assertEquals(actual.serviceName, OK_DTO.serviceName);
		Assert.assertEquals(actual.serverName, OK_DTO.serverName);
		Assert.assertEquals(actual.name, OK_DTO.name);
		Assert.assertEquals(actual.ulid, OK_DTO.ulid);
		Assert.assertEquals(actual.value, OK_DTO.value);
	}

	private GaugeBinaryDto makeBlob(String service, String server, String name, String ulid, Long value, String apikey){
		var gaugeDto = new GaugeDto(name, service, server, ulid, value);
		var batchDto = new GaugeBatchDto(List.of(gaugeDto));
		return GaugeBinaryDto.createSizedDtos(batchDto, apikey, 100).get(0);
	}

}
