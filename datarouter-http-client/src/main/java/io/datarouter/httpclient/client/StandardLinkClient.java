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
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;

import io.datarouter.httpclient.endpoint.link.BaseLink;
import io.datarouter.httpclient.endpoint.link.LinkTool;
import io.datarouter.httpclient.endpoint.link.LinkType;
import jakarta.inject.Singleton;

@Singleton
public class StandardLinkClient<
		L extends LinkType>
implements LinkClient<L>{

	private final CloseableHttpClient httpClient;
	private final Supplier<URI> urlPrefix;

	StandardLinkClient(
			CloseableHttpClient httpClient,
			Supplier<URI> urlPrefix){
		this.httpClient = httpClient;
		this.urlPrefix = urlPrefix;
	}

	public StandardLinkClient(StandardDatarouterHttpClient client){
		this(
				client.httpClient,
				client.urlPrefix);
	}

	@Override
	public String toUrl(BaseLink<L> link){
		try{
			link.setUrlPrefix(urlPrefix.get());
			URIBuilder prefixBuilder = new URIBuilder(link.urlPrefix);
			URIBuilder uriBuilder = new URIBuilder();
			uriBuilder.setScheme(prefixBuilder.getScheme());
			uriBuilder.setHost(prefixBuilder.getHost());
			uriBuilder.setPort(prefixBuilder.getPort());
			// path needs to be set like this to properly encode path segments
			uriBuilder.setPath((prefixBuilder.getPath() != null
					? prefixBuilder.getPath() : "") + link.pathNode.toSlashedString());
			Map<String,String> paramMap = LinkTool.getParamFields(link);
			paramMap.forEach(uriBuilder::addParameter);
			List<NameValuePair> listParams = LinkTool.getNameValueListParamFields(link);
			uriBuilder.addParameters(listParams);
			return uriBuilder.build().toString();
		}catch(URISyntaxException e){
			throw new RuntimeException(e);
		}
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
