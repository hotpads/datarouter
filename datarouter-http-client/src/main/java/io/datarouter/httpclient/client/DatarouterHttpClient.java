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

import java.lang.reflect.Type;
import java.util.function.Consumer;

import org.apache.http.HttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import io.datarouter.httpclient.request.DatarouterHttpRequest;
import io.datarouter.httpclient.response.Conditional;
import io.datarouter.httpclient.response.DatarouterHttpResponse;
import io.datarouter.httpclient.response.exception.DatarouterHttpException;
import io.datarouter.json.JsonSerializer;

public interface DatarouterHttpClient extends HttpPoolStats{

	DatarouterHttpResponse execute(DatarouterHttpRequest request);
	DatarouterHttpResponse execute(DatarouterHttpRequest request, Consumer<HttpEntity> httpEntityConsumer);
	<E> E execute(DatarouterHttpRequest request, Type deserializeToType);

	<E> E executeChecked(DatarouterHttpRequest request, Type deserializeToType) throws DatarouterHttpException;
	DatarouterHttpResponse executeChecked(DatarouterHttpRequest request) throws DatarouterHttpException;
	DatarouterHttpResponse executeChecked(DatarouterHttpRequest request, Consumer<HttpEntity> httpEntityConsumer)
	throws DatarouterHttpException;

	Conditional<DatarouterHttpResponse> tryExecute(DatarouterHttpRequest request);
	<E> Conditional<E> tryExecute(DatarouterHttpRequest request, Type deserializeToType);
	Conditional<DatarouterHttpResponse> tryExecute(
			DatarouterHttpRequest request,
			Consumer<HttpEntity> httpEntityConsumer);

	void shutdown();

	DatarouterHttpClient addDtoToPayload(DatarouterHttpRequest request, Object dto, String dtoType);
	DatarouterHttpClient setEntityDto(DatarouterHttpRequest request, Object dto);

	CloseableHttpClient getApacheHttpClient();
	JsonSerializer getJsonSerializer();

}
