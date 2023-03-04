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
package io.datarouter.web.warmup;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.DatarouterServicePaths;
import io.datarouter.httpclient.request.DatarouterHttpRequest;
import io.datarouter.httpclient.request.HttpRequestMethod;
import io.datarouter.instrumentation.count.Counters;
import io.datarouter.web.config.DatarouterWebSettingRoot;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.port.CompoundPortIdentifier;

@Singleton
public class HttpWarmup{
	private static final Logger logger = LoggerFactory.getLogger(HttpWarmup.class);

	public static final String PARAM_HEALTHCHECK_MODE = "mode";
	public static final String MONITOR_CONFIG_NAME = "monitor";

	@Inject
	private HttpWarmupHttpClient httpWarmupHttpClient;
	@Inject
	private CompoundPortIdentifier compoundPortIdentifier;
	@Inject
	private DatarouterServicePaths datarouterServicePaths;
	@Inject
	private ServletContextSupplier servletContextSupplier;
	@Inject
	private DatarouterWebSettingRoot datarouterWebSettings;

	public void makeHttpWamrupCalls(){
		if(!datarouterWebSettings.httpWarmup.get()){
			return;
		}
		logger.warn("starting warmup http calls");
		for(int i = 0; i < datarouterWebSettings.httpWarmupIteration.get(); i++){
			String url = "https://localhost:" + compoundPortIdentifier.getHttpsPort()
					+ servletContextSupplier.getContextPath()
					+ datarouterServicePaths.datarouter.healthcheck.toSlashedString()
					+ "?" + PARAM_HEALTHCHECK_MODE + "=" + MONITOR_CONFIG_NAME;
			var request = new DatarouterHttpRequest(HttpRequestMethod.GET, url);
			httpWarmupHttpClient.execute(request);
			count("httpCall");
		}
	}

	private void count(String string){
		Counters.inc("Warmup " + string);
	}

}
