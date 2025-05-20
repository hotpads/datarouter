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

public class DocumentedResponseJspDto{

	private final String type;
	private final String example;
	private final String enumValuesDisplay;
	private final ApiDocSchemaDto schema;

	public DocumentedResponseJspDto(String type, String example, String enumValuesDisplay, ApiDocSchemaDto schema){
		this.type = type;
		this.example = example;
		this.enumValuesDisplay = enumValuesDisplay;
		this.schema = schema;
	}

	public String getType(){
		return type;
	}

	public String getExample(){
		return example;
	}

	public String getEnumValuesDisplay(){
		return enumValuesDisplay;
	}

	public ApiDocSchemaDto getSchema(){
		return schema;
	}
}
