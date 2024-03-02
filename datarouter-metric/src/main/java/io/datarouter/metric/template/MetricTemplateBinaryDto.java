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
package io.datarouter.metric.template;

import java.util.List;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.BinaryDto;
import io.datarouter.binarydto.dto.BinaryDtoField;
import io.datarouter.instrumentation.metric.collector.MetricTemplateDto;
import io.datarouter.instrumentation.metric.collector.MetricTemplateDto.Nested.MetricTemplateNodeDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.Require;

public class MetricTemplateBinaryDto extends BinaryDto<MetricTemplateBinaryDto>{

	@BinaryDtoField(index = 0)
	public final String serviceName;
	@BinaryDtoField(index = 1)
	public final List<MetricTemplatePartBinaryDto> parts;
	@BinaryDtoField(index = 2)
	public final String description;

	public MetricTemplateBinaryDto(String serviceName, MetricTemplateDto pattern){
		this.serviceName = Require.notBlank(serviceName);
		this.parts = Scanner.of(pattern.nodes())
				.map(node -> new MetricTemplatePartBinaryDto(
						node.isVariable(),
						node.text(),
						node.description()))
				.list();
		this.description = pattern.description();
	}

	public MetricTemplatePublishedDto toDto(){
		List<MetricTemplateNodeDto> partDtos = Scanner.of(parts)
				.map(MetricTemplatePartBinaryDto::toDto)
				.list();
		return new MetricTemplatePublishedDto(serviceName, new MetricTemplateDto(partDtos, description));
	}

	public static MetricTemplateBinaryDto decode(byte[] bytes){
		return BinaryDtoIndexedCodec.of(MetricTemplateBinaryDto.class).decode(bytes);
	}

	public static class MetricTemplatePartBinaryDto extends BinaryDto<MetricTemplatePartBinaryDto>{

		@BinaryDtoField(index = 0)
		public final boolean isVariable;
		@BinaryDtoField(index = 1)
		public final String text;
		@BinaryDtoField(index = 2)
		public final String description;

		public MetricTemplatePartBinaryDto(boolean isVariable, String text, String description){
			this.isVariable = isVariable;
			this.text = Require.notBlank(text);
			this.description = description;
		}

		public MetricTemplateNodeDto toDto(){
			return new MetricTemplateNodeDto(isVariable, text, description);
		}

	}

}
