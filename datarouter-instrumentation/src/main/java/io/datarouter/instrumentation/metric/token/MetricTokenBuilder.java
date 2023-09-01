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
package io.datarouter.instrumentation.metric.token;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.datarouter.instrumentation.metric.MetricRecorder;
import io.datarouter.instrumentation.metric.collector.MetricTemplateDto;
import io.datarouter.instrumentation.metric.collector.MetricTemplateDto.Nested.MetricTemplateNodeDto;
import io.datarouter.instrumentation.metric.token.MetricToken.MetricLiteral;

public class MetricTokenBuilder extends MetricRecorder{

	private final List<MetricToken> nodes;

	protected MetricTokenBuilder(MetricToken root){
		nodes = new ArrayList<>();
		nodes.add(root);
	}

	public MetricTokenBuilder and(MetricToken node){
		nodes.add(node);
		return this;
	}

	public MetricTokenBuilder and(String literal){
		nodes.add(new MetricLiteral(literal));
		return this;
	}

	@Override
	protected MetricTemplateDto makePatternDto(String description){
		List<MetricTemplateNodeDto> nodeDtos = nodes.stream()
				.map(MetricToken::toDto)
				.toList();
		return new MetricTemplateDto(nodeDtos, description);
	}

	@Override
	public String toMetricName(){
		return nodes.stream()
				.map(MetricToken::token)
				.collect(Collectors.joining(MetricToken.DELIMITER));
	}


}
