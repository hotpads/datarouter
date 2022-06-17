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

import java.util.List;

import io.datarouter.storage.client.ClientId;

public interface AuroraClientIdProvider{

	List<AuroraClientDto> getAuroraClientDtos();

	class GenericAuroraClientIdProvider implements AuroraClientIdProvider{

		private final List<AuroraClientDto> auroraClientDtos;

		public GenericAuroraClientIdProvider(List<AuroraClientDto> auroraClientDtos){
			this.auroraClientDtos = auroraClientDtos;
		}

		@Override
		public List<AuroraClientDto> getAuroraClientDtos(){
			return auroraClientDtos;
		}

	}

	public static class AuroraClientDto{

		private final ClientId writerClientId;
		private final List<ClientId> readerClientIds;
		private final String writerDns;
		private final List<String> readerDnss;
		private final String otherName;
		private final String otherDns;
		private final String clusterName;


		public AuroraClientDto(ClientId writerClientId, List<ClientId> readerClientIds, String writerDns,
				List<String> readerDnss, String otherName, String otherDns, String clusterName){
			this.writerClientId = writerClientId;
			this.readerClientIds = readerClientIds;
			this.writerDns = writerDns;
			this.readerDnss = readerDnss;
			this.otherName = otherName;
			this.otherDns = otherDns;
			this.clusterName = clusterName;
		}

		public ClientId getWriterClientId(){
			return writerClientId;
		}

		public List<ClientId> getReaderClientIds(){
			return readerClientIds;
		}

		public String getWriterDns(){
			return writerDns;
		}

		public List<String> getReaderDnss(){
			return readerDnss;
		}

		public String getClusterName(){
			return clusterName;
		}

		public String getOtherName(){
			return otherName;
		}

		public String getOtherDns(){
			return otherDns;
		}


	}

}
