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
package io.datarouter.web.http;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.gson.GsonJsonSerializer;
import io.datarouter.httpclient.client.BaseDatarouterHttpClientWrapper;
import io.datarouter.httpclient.client.DatarouterHttpClientBuilder;
import io.datarouter.httpclient.request.DatarouterHttpRequest;
import io.datarouter.httpclient.request.HttpRequestMethod;
import io.datarouter.util.concurrent.ThreadTool;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.web.handler.BaseHandler;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

public class HttpReuseTesterHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(HttpReuseTesterHandler.class);

	@Inject
	private HttpReuseTesterClient httpReuseTesterClient;

	@Handler
	public Object reuseTester(String url, Optional<Integer> waitMultiplier){
		var request = new DatarouterHttpRequest(HttpRequestMethod.GET, url);
		int waitTimeS = 1;
		do{
			var response = httpReuseTesterClient.execute(request);
			waitTimeS *= waitMultiplier.orElse(2);
			logger.warn("got {} will wait {}",
					response.getStatusCode(),
					new DatarouterDuration(waitTimeS,TimeUnit.SECONDS));
			ThreadTool.sleepUnchecked(waitTimeS * 1_000);
		}while(true);
	}

	@Singleton
	public static class HttpReuseTesterClient extends BaseDatarouterHttpClientWrapper{

		public HttpReuseTesterClient(){
			super(new DatarouterHttpClientBuilder("http-reuse-tester-client", GsonJsonSerializer.DEFAULT)
					.disableRedirectHandling()
					.setMaxConnectionsPerRoute(1)
					.build());
		}

	}

}
