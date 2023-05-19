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
package io.datarouter.web.plugins.forwarder;

import static j2html.TagCreator.div;
import static j2html.TagCreator.span;

import java.net.URL;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import io.datarouter.util.Require;
import io.datarouter.util.net.UrlTool;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.specialized.DivTag;

@Singleton
public class ForwarderHandlerPage{

	private static final List<String> CALLBACK_PROTOCOLS = List.of("http", "https");

	@Inject
	private Bootstrap4PageFactory bootstrap4PageFactory;

	public Mav handler(HttpServletRequest request, String callbackUrl){
		validateCallbackUrl(request, callbackUrl);
		return bootstrap4PageFactory.startBuilder(request)
				.withTitle("Redirecting")
				.withHttpEquiv("Refresh", "0; " + callbackUrl)
				.withContent(makeContent(callbackUrl))
				.buildMav();
	}

	private static DivTag makeContent(String callbackUrl){
		return div().withClass("container my-5")
				.with(div().withClass("spinner-border"))
				.with(span(callbackUrl + " is loading"));
	}

	private void validateCallbackUrl(HttpServletRequest request, String callbackUrl){
		String currentHost = request.getServerName();
		validateCallbackUrl(callbackUrl, currentHost);
	}

	protected static void validateCallbackUrl(String callbackUrl, String currentHost){
		if(callbackUrl.startsWith("/")){
			return;
		}
		URL url = UrlTool.create(callbackUrl);
		Require.contains(CALLBACK_PROTOCOLS, url.getProtocol());
		Require.contains(List.of(currentHost, "localhost"), url.getHost());
	}

}
