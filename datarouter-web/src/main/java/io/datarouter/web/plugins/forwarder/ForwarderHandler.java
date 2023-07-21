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

import java.util.List;
import java.util.Optional;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.plugins.forwarder.ForwarderPlugin.ForwarderPluginInterceptor;
import jakarta.inject.Inject;

public class ForwarderHandler extends BaseHandler{

	@Inject
	private ForwarderHandlerPage forwarderHandlerPage;
	@Inject
	private List<Class<? extends ForwarderPluginInterceptor>> interceptors;
	@Inject
	private DatarouterInjector datarouterInjector;

	@Handler(defaultHandler = true)
	public Mav handler(String protocol, Optional<String> hostname, int port, String path){
		String callbackUrl = protocol + "://" + hostname.orElse("localhost") + ":" + port + path;

		for(Class<? extends ForwarderPluginInterceptor> interceptorClass : interceptors){
			ForwarderPluginInterceptor interceptor = datarouterInjector.getInstance(interceptorClass);
			interceptor.accept(request, response);
		}

		return forwarderHandlerPage.handler(request, callbackUrl);
	}

}
