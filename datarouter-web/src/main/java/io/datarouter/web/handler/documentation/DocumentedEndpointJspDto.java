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

import java.util.List;

public class DocumentedEndpointJspDto{

	private final String url;
	private final String implementation;
	private final List<DocumentedParameterJspDto> parameters;
	private final String apiKeyFieldName;
	private final String description;
	private final DocumentedResponseJspDto response;
	private final boolean isDeprecated;
	private final List<DocumentedErrorJspDto> errors;
	private final String paramsEnumValuesDisplay;
	private final String callerType;

	public DocumentedEndpointJspDto(
			String url,
			String implementation,
			List<DocumentedParameterJspDto> parameters,
			String apiKeyFieldName,
			String description,
			DocumentedResponseJspDto response,
			boolean isDeprecated,
			List<DocumentedErrorJspDto> errors,
			String paramsEnumValuesDisplay,
			String callerType){
		this.url = url;
		this.implementation = implementation;
		this.parameters = parameters;
		this.apiKeyFieldName = apiKeyFieldName;
		this.description = description;
		this.response = response;
		this.isDeprecated = isDeprecated;
		this.errors = errors;
		this.paramsEnumValuesDisplay = paramsEnumValuesDisplay;
		this.callerType = callerType;
	}

	public String getUrl(){
		return url;
	}

	public String getImplementation(){
		return implementation;
	}

	public List<DocumentedParameterJspDto> getParameters(){
		return parameters;
	}

	public String getApiKeyFieldName(){
		return apiKeyFieldName;
	}

	public String getDescription(){
		return description;
	}

	public DocumentedResponseJspDto getResponse(){
		return response;
	}

	public boolean getIsDeprecated(){
		return isDeprecated;
	}

	public List<DocumentedErrorJspDto> getErrors(){
		return errors;
	}

	public String getParamsEnumValuesDisplay(){
		return paramsEnumValuesDisplay;
	}

	public String getCallerType(){
		return callerType;
	}

}
