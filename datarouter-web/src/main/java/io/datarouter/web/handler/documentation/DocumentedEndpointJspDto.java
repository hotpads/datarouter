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
	private final String deprecatedOn;
	private final String deprecationLink;
	private final List<DocumentedErrorJspDto> errors;
	private final String paramsEnumValuesDisplay;
	private final String requestType;
	private final String newWebEndpointPath;
	private final String newMobileEndpointPath;
	private final String newServiceHref;

	public DocumentedEndpointJspDto(
			String url,
			String implementation,
			List<DocumentedParameterJspDto> parameters,
			String apiKeyFieldName,
			String description,
			DocumentedResponseJspDto response,
			boolean isDeprecated,
			String deprecatedOn,
			String deprecationLink,
			List<DocumentedErrorJspDto> errors,
			String paramsEnumValuesDisplay,
			String requestType,
			String newWebEndpointPath,
			String newMobileEndpointPath,
			String newServiceHref){
		this.url = url;
		this.implementation = implementation;
		this.parameters = parameters;
		this.apiKeyFieldName = apiKeyFieldName;
		this.description = description;
		this.response = response;
		this.isDeprecated = isDeprecated;
		this.deprecatedOn = deprecatedOn;
		this.deprecationLink = deprecationLink;
		this.errors = errors;
		this.paramsEnumValuesDisplay = paramsEnumValuesDisplay;
		this.requestType = requestType;
		this.newWebEndpointPath = newWebEndpointPath;
		this.newMobileEndpointPath = newMobileEndpointPath;
		this.newServiceHref = newServiceHref;
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

	public String getDeprecatedOn(){
		return deprecatedOn;
	}

	public String getDeprecationLink(){
		return deprecationLink;
	}

	public List<DocumentedErrorJspDto> getErrors(){
		return errors;
	}

	public String getParamsEnumValuesDisplay(){
		return paramsEnumValuesDisplay;
	}

	public String getRequestType(){
		return requestType;
	}

	public boolean hasRequestBody(){
		return parameters.stream()
				.anyMatch(DocumentedParameterJspDto::getRequestBody);
	}

	public String getNewWebEndpointPath(){
		return newWebEndpointPath;
	}

	public String getNewMobileEndpointPath(){
		return newMobileEndpointPath;
	}

	public String getNewServiceHref(){
		return newServiceHref;
	}
}
