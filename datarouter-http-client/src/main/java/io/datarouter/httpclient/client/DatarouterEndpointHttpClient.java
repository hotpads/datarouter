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

import io.datarouter.httpclient.endpoint.BaseEndpoint;
import io.datarouter.httpclient.endpoint.EndpointType;
import io.datarouter.httpclient.json.JsonSerializer;
import io.datarouter.httpclient.response.Conditional;
import io.datarouter.httpclient.response.exception.DatarouterHttpException;

public interface DatarouterEndpointHttpClient<ET extends EndpointType>{

	<R> Conditional<R> call(BaseEndpoint<R,ET> endpoint);
	<R> Conditional<R> callAnyType(BaseEndpoint<R,?> endpoint);
	<R> R callChecked(BaseEndpoint<R,ET> endpoint) throws DatarouterHttpException;

	String toUrl(BaseEndpoint<?,ET> endpoint);

	void shutdown();

	PoolStats getPoolStats();
	CloseableHttpClient getApacheHttpClient();
	JsonSerializer getJsonSerializer();

	void initUrlPrefix(BaseEndpoint<?,ET> endpoint);

}
