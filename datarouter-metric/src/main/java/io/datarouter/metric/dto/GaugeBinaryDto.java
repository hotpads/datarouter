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

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.BinaryDto;
import io.datarouter.binarydto.dto.BinaryDtoField;
import io.datarouter.instrumentation.gauge.GaugeBatchDto;
import io.datarouter.instrumentation.gauge.GaugeDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.Require;

public class GaugeBinaryDto extends BinaryDto<GaugeBinaryDto>{

	@BinaryDtoField(index = 0)
	public final String serviceName;
	@BinaryDtoField(index = 1)
	public final String serverName;
	@BinaryDtoField(index = 2)
	public final List<GaugeItemBinaryDto> items;
	@BinaryDtoField(index = 3)
	public final String apiKey;

	private GaugeBinaryDto(String serviceName, String serverName, List<GaugeItemBinaryDto> items){
		this.serviceName = Require.notBlank(serviceName);
		this.serverName = Require.notBlank(serverName);
		this.items = items;
		this.apiKey = null;//removing this causes deserialization issues
	}

	public List<GaugeDto> toGaugeDtos(){
		return Scanner.of(items)
				.map(item -> new GaugeDto(item.name, serviceName, serverName, item.ulid, item.value))
				.list();
	}

	public static List<GaugeBinaryDto> createSizedDtos(GaugeBatchDto gaugeBatchDto, int itemsPerDto){
		Require.notNull(gaugeBatchDto);
		Require.notEmpty(gaugeBatchDto.batch);

		return Scanner.of(gaugeBatchDto.batch)
				.map(GaugeItemBinaryDto::new)
				.batch(itemsPerDto)
				.map(items -> new GaugeBinaryDto(
						gaugeBatchDto.batch.get(0).serviceName,
						gaugeBatchDto.batch.get(0).serverName,
						items))
				.list();
	}

	public static GaugeBinaryDto decode(byte[] bytes){
		return BinaryDtoIndexedCodec.of(GaugeBinaryDto.class).decode(bytes);
	}

	//TODO make private and test if binary dto still works
	public static class GaugeItemBinaryDto extends BinaryDto<GaugeItemBinaryDto>{

		@BinaryDtoField(index = 0)
		public final String name;
		@BinaryDtoField(index = 1)
		public final String ulid;
		@BinaryDtoField(index = 2)
		public final Long value;

		public GaugeItemBinaryDto(String name, String ulid, Long value){
			this.name = Require.notBlank(name);
			this.ulid = Require.notBlank(ulid);
			this.value = Require.notNull(value);
		}

		public GaugeItemBinaryDto(GaugeDto gaugeDto){
			this(gaugeDto.name, gaugeDto.ulid, gaugeDto.value);
		}

	}

}
