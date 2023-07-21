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
package io.datarouter.web.handler.mav;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;

import io.datarouter.util.singletonsupplier.SingletonSupplier;
import io.datarouter.web.html.PageScripts;
import io.datarouter.web.listener.TomcatWebAppNamesWebAppListener;
import io.datarouter.web.navigation.AppNavBar;
import io.datarouter.web.navigation.DatarouterNavBar;
import io.datarouter.web.util.RequestAttributeKey;
import io.datarouter.web.util.RequestAttributeTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class MavPropertiesFactory{

	private static final RequestAttributeKey<MavProperties> MAV_PROPERTIES = new RequestAttributeKey<>("mavProperties");

	private final DatarouterMavPropertiesFactoryConfig config;
	private final Supplier<Map<String,String>> tomcatWebApps;
	private final Optional<AppNavBar> appNavBar;
	private final PageScripts pageScripts;
	private final DatarouterNavBar datarouterNavBar;

	@Inject
	public MavPropertiesFactory(
			TomcatWebAppNamesWebAppListener webAppsListener,
			DatarouterMavPropertiesFactoryConfig config,
			Optional<AppNavBar> appNavBar,
			PageScripts pageScripts,
			DatarouterNavBar datarouterNavBar){
		this.config = config;
		this.tomcatWebApps = SingletonSupplier.of(webAppsListener::getTomcatWebApps);
		this.appNavBar = appNavBar;
		this.pageScripts = pageScripts;
		this.datarouterNavBar = datarouterNavBar;
	}

	// called by prelude.jspf
	public MavProperties buildAndSet(HttpServletRequest request){
		MavProperties mavProperties = new MavProperties(
				request,
				config.getCssVersion(),
				config.getJsVersion(),
				config.hasAnyDatarouterPrivileges(request),
				tomcatWebApps.get(),
				appNavBar,
				config.getIsProduction(),
				datarouterNavBar,
				pageScripts);
		return RequestAttributeTool.set(request, MAV_PROPERTIES, mavProperties);
	}

	public MavProperties getExistingOrNew(HttpServletRequest request){
		return RequestAttributeTool.get(request, MAV_PROPERTIES).orElseGet(() -> buildAndSet(request));
	}

}
