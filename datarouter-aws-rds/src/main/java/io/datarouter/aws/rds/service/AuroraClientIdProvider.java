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
package io.datarouter.aws.rds.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;

public interface AuroraClientIdProvider{

	List<AuroraClientDto> getAuroraClientDtos();
	Optional<AuroraClientDto> findAuroraClientDtoForClientName(String clientName);

	class GenericAuroraClientIdProvider implements AuroraClientIdProvider{

		private final Map<String,AuroraClientDto> auroraClientDtoMap = new HashMap<>();
		private final List<AuroraClientDto> auroraClientDtos;

		public GenericAuroraClientIdProvider(List<AuroraClientDto> auroraClientDtos){
			this.auroraClientDtos = auroraClientDtos;
			Scanner.of(auroraClientDtos).forEach(dto -> {
				this.auroraClientDtoMap.put(dto.writerClientId.getName(), dto);
			});
		}

		@Override
		public List<AuroraClientDto> getAuroraClientDtos(){
			return auroraClientDtos;
		}

		@Override
		public Optional<AuroraClientDto> findAuroraClientDtoForClientName(String clientName){
			return Optional.ofNullable(auroraClientDtoMap.get(clientName));
		}

	}

	public record AuroraClientDto(
			ClientId writerClientId,
			List<ClientId> readerClientIds,
			String writerDns,
			List<String> readerDnss,
			String otherName,
			String otherDns,
			String clusterName,
			String instanceType,
			String availabilityZone){
	}

}
