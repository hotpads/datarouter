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

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.bytes.HexBlockTool;
import io.datarouter.instrumentation.gauge.GaugeBatchDto;
import io.datarouter.instrumentation.gauge.GaugeDto;
import io.datarouter.metric.dto.RawGaugeBinaryDto.GaugeItemBinaryDto;
import io.datarouter.types.Ulid;

public class RawGaugeBinaryDtoTests{

	private static final String
			NAME = "name",
			ULID = new Ulid().value(),
			SERVICE = "service",
			SERVER = "server";

	private static final GaugeDto OK_DTO = new GaugeDto(NAME, SERVICE, SERVER, ULID, 1L);

	private static final BinaryDtoIndexedCodec<RawGaugeBinaryDto> CODEC = BinaryDtoIndexedCodec.of(
			RawGaugeBinaryDto.class);

	@Test
	public void testParse20230201(){
		var expected = new RawGaugeBinaryDto(
				"myService",
				"myServer",
				List.of(new GaugeItemBinaryDto("myGauge", "01GRCXSMZBFNW5DTTWFFB9SJ3X", 3L)));
//		HexBlockTool.print(expected.encodeIndexed());
		String hex = """
				00096d795365727669636501086d79536572766572023201012f00076d794761756765011a303147
				524358534d5a42464e57354454545746464239534a335802088000000000000003
				""";
		var actual = RawGaugeBinaryDto.decode(HexBlockTool.fromHexBlock(hex));
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testConstructorValidation(){
		var dto = makeDto(SERVICE, SERVER, NAME, ULID, 1L);
		verifyOkDto(dto);

		Assert.assertThrows(
				IllegalArgumentException.class,
				() -> makeDto("", SERVER, NAME, ULID, 1L));
		Assert.assertThrows(
				IllegalArgumentException.class,
				() ->
				makeDto(null, SERVER, NAME, ULID, 1L));

		Assert.assertThrows(
				IllegalArgumentException.class,
				() -> makeDto(SERVICE, "", NAME, ULID, 1L));
		Assert.assertThrows(
				IllegalArgumentException.class,
				() -> makeDto(SERVICE, null, NAME, ULID, 1L));

		Assert.assertThrows(
				IllegalArgumentException.class,
				() -> makeDto(SERVICE, SERVER, "", ULID, 1L));
		Assert.assertThrows(
				IllegalArgumentException.class,
				() -> makeDto(SERVICE, SERVER, null, ULID, 1L));

		Assert.assertThrows(
				IllegalArgumentException.class,
				() -> makeDto(SERVICE, SERVER, NAME, "", 1L));
		Assert.assertThrows(
				IllegalArgumentException.class,
				() -> makeDto(SERVICE, SERVER, NAME, null, 1L));

		Assert.assertThrows(
				IllegalArgumentException.class,
				() -> makeDto(SERVICE, SERVER, NAME, ULID, null));

		Assert.assertThrows(
				IllegalArgumentException.class,
				() -> RawGaugeBinaryDto.createSizedDtos(null, Integer.MAX_VALUE).get(0));

		var nullBatch = new GaugeBatchDto(null);
		Assert.assertThrows(
				IllegalArgumentException.class,
				() -> RawGaugeBinaryDto.createSizedDtos(nullBatch, Integer.MAX_VALUE).get(0));

		var emptyBatch = new GaugeBatchDto(List.of());
		Assert.assertThrows(
				IllegalArgumentException.class,
				() -> RawGaugeBinaryDto.createSizedDtos(emptyBatch, Integer.MAX_VALUE).get(0));
	}

	@Test
	public void testSingleRoundTrip(){
		var dto = makeDto(SERVICE, SERVER, NAME, ULID, 1L);
		verifyOkDto(CODEC.decode(CODEC.encode(dto)));
	}

	@Test
	public void testCreateSizeDtosAll(){
		int numCounts = 50;
		var dtos = new ArrayList<GaugeDto>();
		for(int i = 0; i < numCounts; i++){
			dtos.add(OK_DTO);
		}
		var batch = new GaugeBatchDto(dtos);
		Assert.assertEquals(RawGaugeBinaryDto.createSizedDtos(batch, numCounts).get(0).items.size(), numCounts);
		Assert.assertEquals(RawGaugeBinaryDto.createSizedDtos(batch, dtos.size()).get(0).items.size(), numCounts);
	}

	private void verifyOkDto(RawGaugeBinaryDto dto){
		var actual = dto.toGaugeDtos().get(0);
		Assert.assertEquals(actual.serviceName(), OK_DTO.serviceName());
		Assert.assertEquals(actual.serverName(), OK_DTO.serverName());
		Assert.assertEquals(actual.name(), OK_DTO.name());
		Assert.assertEquals(actual.ulid(), OK_DTO.ulid());
		Assert.assertEquals(actual.value(), OK_DTO.value());
	}

	private RawGaugeBinaryDto makeDto(String service, String server, String name, String ulid, Long value){
		var gaugeDto = new GaugeDto(name, service, server, ulid, value);
		var batchDto = new GaugeBatchDto(List.of(gaugeDto));
		return RawGaugeBinaryDto.createSizedDtos(batchDto, 100).get(0);
	}

}
