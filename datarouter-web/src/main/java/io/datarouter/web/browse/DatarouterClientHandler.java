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
package io.datarouter.web.browse;

import javax.inject.Inject;

import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.InContextRedirectMav;
import io.datarouter.web.handler.mav.imp.MessageMav;

public class DatarouterClientHandler extends BaseHandler{

	@Inject
	private DatarouterClientWebInspectorRegistry datarouterClientWebInspectorRegistry;
	@Inject
	private DatarouterClients datarouterClients;
	@Inject
	private DatarouterWebPaths paths;

	@Handler
	public Mav inspectClient(String clientName){
		String clientType = datarouterClients.getClientTypeInstance(datarouterClients.getClientId(clientName))
				.getName();
		return datarouterClientWebInspectorRegistry.get(clientType)
				.map(inspector -> inspector.inspectClient(params, request))
				.orElseGet(() -> new MessageMav("Can't inspect " + clientType + ". Make sure it registers a "
						+ DatarouterClientWebInspector.class.getSimpleName() + " in "
						+ DatarouterClientWebInspectorRegistry.class.getSimpleName() + "."));
	}

	@Handler
	public Mav initClient(String clientName){
		ClientId clientId = datarouterClients.getClientId(clientName);
		datarouterClients.getClientManager(clientId).initClient(clientId);
		return new InContextRedirectMav(request, paths.datarouter);
	}

	@Handler
	public Mav initAllClients(){
		datarouterClients.initAllClients();
		return new InContextRedirectMav(request, paths.datarouter);
	}

}
