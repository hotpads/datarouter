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
package io.datarouter.relay;

import io.datarouter.instrumentation.relay.dto.RelayAddToThreadRequestDto;
import io.datarouter.instrumentation.relay.dto.RelayMessageResponseDto;
import io.datarouter.instrumentation.relay.dto.RelayStartThreadRequestDto;
import jakarta.inject.Singleton;

public interface DatarouterRelaySender{

	RelayMessageResponseDto startThread(RelayStartThreadRequestDto request);
	RelayMessageResponseDto startThreadForceProduction(RelayStartThreadRequestDto request);
	RelayMessageResponseDto addToThread(RelayAddToThreadRequestDto request);

	@Singleton
	class NoOpDatarouterRelaySender implements DatarouterRelaySender{

		@Override
		public RelayMessageResponseDto startThread(RelayStartThreadRequestDto request){
			return null;
		}

		@Override
		public RelayMessageResponseDto startThreadForceProduction(RelayStartThreadRequestDto request){
			return null;
		}

		@Override
		public RelayMessageResponseDto addToThread(RelayAddToThreadRequestDto request){
			return null;
		}

	}

}
