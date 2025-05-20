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

import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;

import io.datarouter.httpclient.endpoint.link.BaseLink;
import io.datarouter.httpclient.endpoint.link.LinkType;

public interface LinkClient<L extends LinkType>{

	String toUrl(BaseLink<L> link);

	default String toInternalUrl(BaseLink<L> link){
		try{
			URIBuilder uriBuilder = new URIBuilder(toUrl(link));
			uriBuilder.setScheme(null);
			uriBuilder.setHost(null);
			return uriBuilder.build().toString();
		}catch(URISyntaxException e){
			throw new RuntimeException(e);
		}
	}

	default String toInternalUrlWithoutContext(BaseLink<L> link){
		return toInternalUrl(link).replaceAll("^/[^/]+/", "/");
	}

	void shutdown();
	void initUrlPrefix(BaseLink<L> link);

}
