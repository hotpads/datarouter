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
package io.datarouter.web.handlerusage;

import java.util.List;

import io.datarouter.scanner.Threads;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.web.handler.BaseHandler.Handler.HandlerUsageType;

public interface HandlerUsageBuilder{
	List<HandlerUsageQueryItemResponseDto> getHandlerUsage(
			List<HandlerUsageQueryItemRequestDto> exactMetricNames,
			String serviceName,
			DatarouterDuration window,
			String datarouterUser,
			Threads threads);

	class NoOpHandlerUsageBuilder implements HandlerUsageBuilder{

		@Override
		public List<HandlerUsageQueryItemResponseDto> getHandlerUsage(
				List<HandlerUsageQueryItemRequestDto> metricItemQueryDto,
				String serviceName,
				DatarouterDuration window,
				String datarouterUser,
				Threads threads){
			return List.of();
		}
	}

	record HandlerUsageQueryItemResponseDto(
			String alias,
			String methodName,
			String classSimpleName,
			Double invocations,
			HandlerUsageType usageType){
	}

	record HandlerUsageQueryItemRequestDto(
			String className,
			String methodName,
			HandlerUsageType usageType,
			String itemId){
	}

}
