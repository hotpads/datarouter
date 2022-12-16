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
package io.datarouter.web.handler.documentation;

import java.util.Set;

import io.datarouter.web.handler.documentation.DocumentedExampleDto.DocumentedExampleEnumDto;

public class DocumentedParameterJspDto{

	private final String name;
	private final String type;
	private final String example;
	private final Boolean required;
	private final Boolean requestBody;
	private final Boolean hidden;
	private final String description;
	private final boolean isDeprecated;

	public final Set<DocumentedExampleEnumDto> exampleEnumDtos;

	public DocumentedParameterJspDto(
			String name,
			String type,
			String example,
			Boolean required,
			Boolean requestBody,
			Boolean hidden,
			String description,
			boolean isDeprecated,
			Set<DocumentedExampleEnumDto> exampleEnumDtos){
		this.name = name;
		this.type = type;
		this.example = example;
		this.required = required;
		this.requestBody = requestBody;
		this.hidden = hidden;
		this.description = description;
		this.isDeprecated = isDeprecated;
		this.exampleEnumDtos = exampleEnumDtos;
	}

	public String getName(){
		return name;
	}

	public String getType(){
		return type;
	}

	public String getExample(){
		return example;
	}

	public Boolean getRequired(){
		return required;
	}

	public Boolean getRequestBody(){
		return requestBody;
	}

	public String getDescription(){
		return description;
	}

	public Boolean getHidden(){
		return hidden;
	}

	public boolean getIsDeprecated(){
		return isDeprecated;
	}

}
