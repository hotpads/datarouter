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

import java.util.Optional;

import io.datarouter.nodewatch.config.DatarouterNodewatchDirectorySupplier;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.file.Directory;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.op.raw.EncodedBlobStorage;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ClientTypeStorageStatsBinaryDao{

	private final Directory directory;
	private final EncodedBlobStorage<ClientTypeStorageStatsBinaryDto> encodedStorage;

	@Inject
	public ClientTypeStorageStatsBinaryDao(
			DatarouterNodewatchDirectorySupplier nodewatchDirectorySupplier){
		directory = nodewatchDirectorySupplier.getStorageStatsClientTypeDirectory();
		encodedStorage = new EncodedBlobStorage<>(directory, ClientTypeStorageStatsBinaryDto.INDEXED_CODEC);
	}

	public void saveClientTypeDto(
			ClientType<?,?> clientType,
			ClientTypeStorageStatsBinaryDto dto){
		encodedStorage.write(PathbeanKey.of(clientType.getName()), dto);
	}

	public Optional<ClientTypeStorageStatsBinaryDto> find(ClientType<?,?> clientType){
		return encodedStorage.find(PathbeanKey.of(clientType.getName()));
	}

}
