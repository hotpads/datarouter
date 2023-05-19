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
package io.datarouter.web.browse;

import static j2html.TagCreator.div;

import javax.inject.Inject;

import io.datarouter.web.browse.components.DatarouterComponentsHtml;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;

public class ViewClientsHandler extends BaseHandler{

	@Inject
	private DatarouterWebPaths paths;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterClientHandlerService clientHandlerService;

	@Handler
	public Mav clients(){
		var header = DatarouterComponentsHtml.makeHeader(
				paths.datarouter.info.clients,
				"Clients",
				"Clients are connections to physical data stores like mysql or memcached");
		var table = clientHandlerService.buildClientsTable();
		var content = div(header, table)
				.withClass("container");
		return pageFactory.startBuilder(request)
				.withTitle("Datarouter Clients")
				.withContent(content)
				.buildMav();
	}

}
