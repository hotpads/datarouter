/**
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
package io.datarouter.httpclient.client;

import io.datarouter.httpclient.dto.DatarouterAccountCredentialStatusDto;
import io.datarouter.httpclient.request.DatarouterHttpRequestBuilder;
import io.datarouter.httpclient.response.Conditional;

public abstract class BaseApplicationHttpClient{

	protected final DatarouterServiceHttpClient httpClient;
	protected final DatarouterHttpRequestBuilder requestBuilder;

	public BaseApplicationHttpClient(DatarouterServiceHttpClient httpClient, DatarouterHttpClientSettings settings){
		this.httpClient = httpClient;
		this.requestBuilder = new DatarouterHttpRequestBuilder(settings, httpClient);
	}

	/**
	 * makes a request to the service's healthcheck API to test its availability
	 * @return successful {@link Conditional} or {@link Conditional#failure(Exception)} as appropriate
	 */
	public Conditional<Object> checkHealth(){
		return httpClient.checkHealth();
	}

	/**
	 * makes a request with the client's configured security params to get their status from the service
	 * (success requires client configuration of apiKey and signature using {@link DatarouterHttpClientBuilder})
	 * @return successful {@link Conditional} or {@link Conditional#failure(Exception)} as appropriate
	 */
	public Conditional<DatarouterAccountCredentialStatusDto> checkCredential(){
		return httpClient.checkCredential();
	}

}
