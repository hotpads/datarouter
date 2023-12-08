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
package io.datarouter.nodewatch.storage.binarydto.storagestats.service;

import java.util.List;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.BinaryDto;
import io.datarouter.binarydto.dto.BinaryDtoField;
import io.datarouter.bytes.Codec;
import io.datarouter.nodewatch.storage.binarydto.storagestats.table.TableStorageStatsBinaryDto;

public class ServiceStorageStatsBinaryDto extends BinaryDto<ServiceStorageStatsBinaryDto>{

	public static final Codec<ServiceStorageStatsBinaryDto,byte[]> INDEXED_CODEC = Codec.of(
			ServiceStorageStatsBinaryDto::encodeIndexed,
			BinaryDtoIndexedCodec.of(ServiceStorageStatsBinaryDto.class)::decode);

	@BinaryDtoField(index = 0)
	public final String serviceName;
	@BinaryDtoField(index = 1)
	public final List<TableStorageStatsBinaryDto> tables;

	public ServiceStorageStatsBinaryDto(
			String serviceName,
			List<TableStorageStatsBinaryDto> tables){
		this.serviceName = serviceName;
		this.tables = tables;
	}

}