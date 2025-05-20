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
package io.datarouter.httpclient.endpoint.link;

import java.util.Map;

import org.apache.http.client.utils.URIBuilder;

import io.datarouter.instrumentation.web.ContextName;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterLinkClient{

	@Inject
	private ContextName contextName;

	public String toInternalUrl(DatarouterLink link){
		var uriBuilder = new URIBuilder()
				.setPath(contextName.getContextPath() + link.pathNode.toSlashedString());
		injectParamMap(link, uriBuilder);
		return uriBuilder.toString();
	}

	public String toInternalUrlWithoutContext(DatarouterLink link){
		var uriBuilder = new URIBuilder()
				.setPath(link.pathNode.toSlashedString());
		injectParamMap(link, uriBuilder);
		return uriBuilder.toString();
	}

	public static String toExternalUrl(String contextPath, DatarouterLink link){
		var uriBuilder = new URIBuilder()
				.setPath(link.pathNode.toSlashedString());
		injectParamMap(link, uriBuilder);
		return contextPath + uriBuilder;
	}

	private static void injectParamMap(DatarouterLink link, URIBuilder uriBuilder){
		Map<String,String> paramMap = LinkTool.getParamFields(link);
		paramMap.forEach(uriBuilder::addParameter);
	}
}
