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
package io.datarouter.instrumentation.webappinstance;

import io.datarouter.instrumentation.response.PublishingResponseDto;

public interface WebappInstancePublisher{

	PublishingResponseDto add(WebappInstanceDto webappInstanceDto);
	PublishingResponseDto delete(String webappName, String serverName, String environment);

	public static class NoOpWebappInstancePublisher implements WebappInstancePublisher{

		@Override
		public PublishingResponseDto add(WebappInstanceDto webappInstanceDto){
			return PublishingResponseDto.NO_OP;
		}

		@Override
		public PublishingResponseDto delete(String webappName, String serverName, String environment){
			return PublishingResponseDto.NO_OP;
		}

	}

}
