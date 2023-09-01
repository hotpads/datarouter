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
package io.datarouter.nodewatch.storage.binarydto.storagestats.clienttype;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.BinaryDto;
import io.datarouter.binarydto.dto.BinaryDtoField;
import io.datarouter.bytes.Codec;

public class ClientTypeStorageStatsBinaryDto extends BinaryDto<ClientTypeStorageStatsBinaryDto>{

	public static final Codec<ClientTypeStorageStatsBinaryDto,byte[]> INDEXED_CODEC = Codec.of(
			ClientTypeStorageStatsBinaryDto::encodeIndexed,
			BinaryDtoIndexedCodec.of(ClientTypeStorageStatsBinaryDto.class)::decode);

	@BinaryDtoField(index = 0)
	public final String clientType;
	@BinaryDtoField(index = 1)
	public final Long totalNameBytes;
	@BinaryDtoField(index = 2)
	public final Long totalValueBytes;

	public ClientTypeStorageStatsBinaryDto(String clientType, Long totalNameBytes, Long totalValueBytes){
		this.clientType = clientType;
		this.totalNameBytes = totalNameBytes;
		this.totalValueBytes = totalValueBytes;
	}

}