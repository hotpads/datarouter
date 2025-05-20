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
package io.datarouter.web.indexusage;

import java.util.List;

import io.datarouter.scanner.Threads;
import io.datarouter.storage.node.op.index.IndexUsage.IndexUsageType;
import io.datarouter.util.duration.DatarouterDuration;

public interface IndexUsageBuilder{

	List<IndexUsageQueryItemResponseDto> getIndexUsage(
			List<IndexUsageQueryItemRequestDto> metricItemQueryDto,
			String serviceName,
			DatarouterDuration window,
			String datarouterUser,
			Threads threads);

	class NoOpIndexUsageBuilder implements IndexUsageBuilder{

		@Override
		public List<IndexUsageQueryItemResponseDto> getIndexUsage(
				List<IndexUsageQueryItemRequestDto> metricItemQueryDto,
				String serviceName,
				DatarouterDuration window,
				String datarouterUser,
				Threads threads){
			return List.of();
		}
	}

	default String buildIndexMetricName(String indexName){
		return "Usage index " + indexName;
	}

	record IndexUsageQueryItemResponseDto(
			Double count,
			IndexUsageType usageType,
			String indexName){
	}

	record IndexUsageQueryItemRequestDto(
			String indexName,
			IndexUsageType usageType,
			String itemId){
	}

}
