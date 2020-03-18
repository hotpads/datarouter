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
package io.datarouter.web.homepage;

import static j2html.TagCreator.div;
import static j2html.TagCreator.h1;

import java.util.List;

import javax.inject.Inject;

import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.util.collection.ListTool;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.navigation.AppNavBarRegistrySupplier;
import io.datarouter.web.navigation.AppPluginNavBarSupplier;
import io.datarouter.web.navigation.NavBarItem;
import io.datarouter.web.navigation.NavBarItem.NavBarItemGroup;

public class AppAndPluginHomepageHandler extends HomepageHandler{

	@Inject
	private DatarouterService datarouterService;
	@Inject
	private Bootstrap4PageFactory factory;
	@Inject
	private AppNavBarRegistrySupplier appNavBarSupplier;
	@Inject
	private AppPluginNavBarSupplier appPluginNavBarSupplier;
	@Inject
	private Bootstrap4CardService cardService;

	@Handler(defaultHandler = true)
	public Mav buildHomepageMav(){
		var h1 = h1(datarouterService.getName()).withClass("text-capitalize");
		var header = div(h1)
				.withClass("container-fluid");
		List<NavBarItem> navBarItems = ListTool.concatenate(appPluginNavBarSupplier.get(), appNavBarSupplier.get());
		var links = cardService.render(NavBarItemGroup.fromNavBarItems(navBarItems));
		var container = div(header, links);
		return factory.startBuilder(request)
				.withTitle(datarouterService.getName())
				.withContent(container)
				.buildMav();
	}

}
