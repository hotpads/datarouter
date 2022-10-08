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
package io.datarouter.httpclient.client;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.pool.PoolStats;

import io.datarouter.httpclient.endpoint.java.BaseEndpoint;
import io.datarouter.httpclient.endpoint.java.EndpointType;
import io.datarouter.httpclient.response.Conditional;
import io.datarouter.httpclient.response.exception.DatarouterHttpException;
import io.datarouter.json.JsonSerializer;

public abstract class BaseDatarouterEndpointClientWrapper<
		ET extends EndpointType>
implements DatarouterEndpointClient<ET>, DatarouterServiceEndpointClient<ET>{

	private final DatarouterEndpointClient<ET> client;

	public BaseDatarouterEndpointClientWrapper(DatarouterEndpointClient<ET> client){
		this.client = client;
	}

	@Override
	public <R> Conditional<R> call(BaseEndpoint<R,ET> baseEndpoint){
		return client.call(baseEndpoint);
	}

	@Override
	public <R> Conditional<R> callAnyType(BaseEndpoint<R,?> baseEndpoint){
		return client.callAnyType(baseEndpoint);
	}

	@Override
	public <R> R callChecked(BaseEndpoint<R,ET> endpoint) throws DatarouterHttpException{
		return client.callChecked(endpoint);
	}

	@Override
	public String toUrl(BaseEndpoint<?,ET> endpoint){
		return client.toUrl(endpoint);
	}

	@Override
	public void shutdown(){
		client.shutdown();
	}

	@Override
	public PoolStats getPoolStats(){
		return client.getPoolStats();
	}

	@Override
	public CloseableHttpClient getApacheHttpClient(){
		return client.getApacheHttpClient();
	}

	@Override
	public JsonSerializer getJsonSerializer(){
		return client.getJsonSerializer();
	}

	@Override
	public void initUrlPrefix(BaseEndpoint<?,ET> endpoint){
		client.initUrlPrefix(endpoint);
	}

}
