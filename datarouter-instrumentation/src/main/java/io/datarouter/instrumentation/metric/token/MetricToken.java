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

import java.util.Optional;

import io.datarouter.instrumentation.metric.collector.MetricTemplateDto.MetricTemplateNodeDto;

public interface MetricToken{

	String DELIMITER = " ";

	String token();
	Optional<MetricTokenVariable> variable();
	MetricTemplateNodeDto toDto();

	default MetricTokenBuilder start(){
		return new MetricTokenBuilder(this);
	}

	public record MetricLiteral(
			String token) implements MetricToken{

		@Override
		public Optional<MetricTokenVariable> variable(){
			return Optional.empty();
		}

		@Override
		public MetricTemplateNodeDto toDto(){
			return new MetricTemplateNodeDto(false, token, null);
		}

	}

	public record MetricTokenVariable(
			String name,
			String description){

		public MetricToken as(String value){
			return new MetricNameVariableNode(this, value);
		}

		private MetricTemplateNodeDto toDto(){
			return new MetricTemplateNodeDto(true, name, description);
		}

		protected static class MetricNameVariableNode implements MetricToken{

			private final MetricTokenVariable variable;
			private final String token;

			public MetricNameVariableNode(MetricTokenVariable variable, String token){
				this.variable = variable;
				this.token = token;
			}

			@Override
			public String token(){
				return token;
			}

			@Override
			public Optional<MetricTokenVariable> variable(){
				return Optional.of(variable);
			}

			@Override
			public MetricTemplateNodeDto toDto(){
				return variable.toDto();
			}

		}

	}

}
