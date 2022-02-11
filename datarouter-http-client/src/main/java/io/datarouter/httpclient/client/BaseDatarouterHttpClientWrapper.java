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
import org.apache.http.pool.PoolStats;

import io.datarouter.httpclient.json.JsonSerializer;
import io.datarouter.httpclient.request.DatarouterHttpRequest;
import io.datarouter.httpclient.response.Conditional;
import io.datarouter.httpclient.response.DatarouterHttpResponse;
import io.datarouter.httpclient.response.exception.DatarouterHttpException;

public abstract class BaseDatarouterHttpClientWrapper implements DatarouterHttpClient{

	private final DatarouterHttpClient datarouterHttpClient;

	public BaseDatarouterHttpClientWrapper(DatarouterHttpClient datarouterHttpClient){
		this.datarouterHttpClient = datarouterHttpClient;
	}

	@Override
	public DatarouterHttpResponse execute(DatarouterHttpRequest request){
		return datarouterHttpClient.execute(request);
	}

	@Override
	public DatarouterHttpResponse execute(DatarouterHttpRequest request, Consumer<HttpEntity> httpEntityConsumer){
		return datarouterHttpClient.execute(request, httpEntityConsumer);
	}

	@Override
	public <E> E execute(DatarouterHttpRequest request, Type deserializeToType){
		return datarouterHttpClient.execute(request, deserializeToType);
	}

	@Override
	public <E> E executeChecked(DatarouterHttpRequest request, Type deserializeToType) throws DatarouterHttpException{
		return datarouterHttpClient.executeChecked(request, deserializeToType);
	}

	@Override
	public DatarouterHttpResponse executeChecked(DatarouterHttpRequest request) throws DatarouterHttpException{
		return datarouterHttpClient.executeChecked(request);
	}

	@Override
	public DatarouterHttpResponse executeChecked(DatarouterHttpRequest request, Consumer<HttpEntity> httpEntityConsumer)
	throws DatarouterHttpException{
		return datarouterHttpClient.executeChecked(request, httpEntityConsumer);
	}

	@Override
	public Conditional<DatarouterHttpResponse> tryExecute(DatarouterHttpRequest request){
		return datarouterHttpClient.tryExecute(request);
	}

	@Override
	public Conditional<DatarouterHttpResponse> tryExecute(
			DatarouterHttpRequest request,
			Consumer<HttpEntity> httpEntityConsumer){
		return datarouterHttpClient.tryExecute(request, httpEntityConsumer);
	}

	@Override
	public <E> Conditional<E> tryExecute(DatarouterHttpRequest request, Type deserializeToType){
		return datarouterHttpClient.tryExecute(request, deserializeToType);
	}

	@Override
	public void shutdown(){
		datarouterHttpClient.shutdown();
	}

	@Override
	public DatarouterHttpClient addDtoToPayload(DatarouterHttpRequest request, Object dto, String dtoType){
		return datarouterHttpClient.addDtoToPayload(request, dto, dtoType);
	}

	@Override
	public DatarouterHttpClient setEntityDto(DatarouterHttpRequest request, Object dto){
		return datarouterHttpClient.setEntityDto(request, dto);
	}

	@Override
	public PoolStats getPoolStats(){
		return datarouterHttpClient.getPoolStats();
	}

	@Override
	public CloseableHttpClient getApacheHttpClient(){
		return datarouterHttpClient.getApacheHttpClient();
	}

	@Override
	public JsonSerializer getJsonSerializer(){
		return datarouterHttpClient.getJsonSerializer();
	}

}
