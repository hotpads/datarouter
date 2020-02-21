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
package io.datarouter.autoconfig.service;

import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;

import io.datarouter.autoconfig.AutoConfigResponse;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.web.app.WebappName;

public abstract class BaseAutoConfigService implements AutoConfigService{

	@Inject
	private ServerTypeDetector serverTypeDetector;
	@Inject
	private WebappName webappName;

	public abstract List<Consumer<AutoConfigResponse>> getAutoConfigAppenders();

	@Override
	public AutoConfigResponse autoConfig(){
		serverTypeDetector.assertNotProductionServer();
		AutoConfigResponse autoConfigResponse = new AutoConfigResponse(webappName.getName());
		getAutoConfigAppenders().forEach(appender -> appender.accept(autoConfigResponse));
		return autoConfigResponse;
	}

}
