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
package io.datarouter.web.monitoring;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.httpclient.client.BaseDatarouterHttpClientWrapper;
import io.datarouter.httpclient.client.DatarouterHttpClientBuilder;
import io.datarouter.httpclient.request.DatarouterHttpRequest;
import io.datarouter.httpclient.request.DatarouterHttpRequest.HttpRequestMethod;
import io.datarouter.httpclient.response.Conditional;

@Singleton
public class OutgoingIpFinderService{

	private static final List<String> IP_SOURCES = List.of(
			"https://checkip.amazonaws.com/",
			"https://ifconfig.me/",
			"https://ipecho.net/plain/",
			"https://ifconfig.co/ip",
			"https://ipinfo.io/ip");

	@Inject
	private OutgoingIpFinderClient ipClient;

	public OutGoingIpWrapper getIpForServer(){
		for(String ipSource : IP_SOURCES){
			Conditional<String> ipResponse = tryGetIp(ipSource);
			if(ipResponse.isFailure()){
				continue;
			}
			String ip = ipResponse.orElse(null);
			if(ip != null){
				return new OutGoingIpWrapper(ipSource, ip);
			}
		}
		return new OutGoingIpWrapper(null, null);
	}

	private Conditional<String> tryGetIp(String url){
		DatarouterHttpRequest request = new DatarouterHttpRequest(HttpRequestMethod.GET, url, false);
		return ipClient.tryExecute(request, String.class);
	}

	public static class OutGoingIpWrapper{

		public final String ipSource;
		public final String ipAddress;

		public OutGoingIpWrapper(String ipSource, String ipAddress){
			this.ipSource = ipSource;
			this.ipAddress = ipAddress;
		}
	}

	@Singleton
	public static class OutgoingIpFinderClient extends BaseDatarouterHttpClientWrapper{

		public OutgoingIpFinderClient(){
			super(new DatarouterHttpClientBuilder().build());
		}

	}

}
