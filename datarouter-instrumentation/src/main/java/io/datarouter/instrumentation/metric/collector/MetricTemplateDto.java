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
package io.datarouter.instrumentation.metric.collector;

import java.util.List;
import java.util.stream.Collectors;

import io.datarouter.instrumentation.metric.collector.MetricTemplateDto.Nested.MetricTemplateNodeDto;
import io.datarouter.instrumentation.metric.token.MetricToken;

public record MetricTemplateDto(
		List<MetricTemplateNodeDto> nodes,
		String description){

	public static class Nested{
		public record MetricTemplateNodeDto(
				boolean isVariable,
				String text,
				String description){
		}
	}

	// Format cannot change, used in databean key
	public String toPatternKey(){
		return nodes.stream()
				.map(node -> node.isVariable() ? "<" + node.text() + ">" : node.text())
				.collect(Collectors.joining(MetricToken.DELIMITER));
	}

}
