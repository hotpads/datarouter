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

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import org.apache.http.impl.client.CloseableHttpClient;

import io.datarouter.httpclient.endpoint.link.BaseLink;
import io.datarouter.httpclient.endpoint.link.LinkTool;
import io.datarouter.httpclient.endpoint.link.LinkType;

@Singleton
public class StandardDatarouterLinkClient<
		L extends LinkType>
implements DatarouterLinkClient<L>{

	private final CloseableHttpClient httpClient;
	private final Supplier<URI> urlPrefix;

	StandardDatarouterLinkClient(
			CloseableHttpClient httpClient,
			Supplier<URI> urlPrefix){
		this.httpClient = httpClient;
		this.urlPrefix = urlPrefix;
	}

	public StandardDatarouterLinkClient(StandardDatarouterHttpClient client){
		this(
				client.httpClient,
				client.urlPrefix);
	}

	@Override
	public String toUrl(BaseLink<L> link){
		link.setUrlPrefix(urlPrefix.get());
		String finalUrl = URI.create(link.urlPrefix + link.pathNode.toSlashedString())
				.normalize()
				.toString();
		Map<String,String> paramMap = LinkTool.getParamFields(link);
		String params = paramMap.entrySet().stream()
				.map(entry -> entry.getKey() + "=" + entry.getValue())
				.collect(Collectors.joining("&", "?", ""));
		return finalUrl + params;
	}

	@Override
	public void shutdown(){
		try{
			httpClient.close();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	@Override
	public void initUrlPrefix(BaseLink<L> link){
		link.setUrlPrefix(urlPrefix.get());
	}

}
