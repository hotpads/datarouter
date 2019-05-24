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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatarouterServiceUnavailableRetryStrategy implements ServiceUnavailableRetryStrategy{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterServiceUnavailableRetryStrategy.class);

	private static final Set<Integer> STATUS_CODES_TO_RETRY = new HashSet<>(Arrays.asList(502, 503, 504));

	private final int retryCount;

	public DatarouterServiceUnavailableRetryStrategy(int retryCount){
		this.retryCount = retryCount;
	}

	@Override
	public boolean retryRequest(HttpResponse response, int executionCount, HttpContext context){
		int statusCode = response.getStatusLine().getStatusCode();
		if(!STATUS_CODES_TO_RETRY.contains(statusCode)){
			return false;
		}
		HttpClientContext clientContext = HttpClientContext.adapt(context);
		boolean willRetry = HttpRetryTool.shouldRetry(context, executionCount, retryCount);
		if(willRetry){
			HttpEntity httpEntity = response.getEntity();
			String entity = HttpRetryTool.entityToString(httpEntity).orElse(null);
			logger.warn("Request {} failure Nº {} statusCode={} entity={}", clientContext.getRequest().getRequestLine(),
					executionCount, statusCode, entity);
		}else{
			// don't log everything, caller will get details in an Exception
			logger.warn("Request {} failure Nº {} (final)", clientContext.getRequest().getRequestLine(),
					executionCount);
		}
		return willRetry;
	}

	@Override
	public long getRetryInterval(){
		return 0;
	}

}
