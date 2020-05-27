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
package io.datarouter.web.html.j2html.bootstrap4;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.li;
import static j2html.TagCreator.text;
import static j2html.TagCreator.ul;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.navigation.AppNavBarRegistrySupplier;
import io.datarouter.web.navigation.AppPluginNavBarSupplier;
import io.datarouter.web.navigation.NavBarItem;
import io.datarouter.web.navigation.NavBarItem.NavBarItemGroup;
import io.datarouter.web.service.ServiceDescriptionSupplier;
import io.datarouter.web.service.ServiceDocumentationNamesAndLinksSupplier;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;

@Singleton
public class Bootstrap4HomepageCreatorService{

	@Inject
	private DatarouterService datarouterService;
	@Inject
	private Bootstrap4PageFactory factory;
	@Inject
	private ServiceDescriptionSupplier serviceDescriptionSupplier;
	@Inject
	private ServiceDocumentationNamesAndLinksSupplier serviceDocNamesAndLinks;
	@Inject
	private AppNavBarRegistrySupplier appNavBarSupplier;
	@Inject
	private AppPluginNavBarSupplier appPluginNavBarSupplier;

	public Mav homepage(HttpServletRequest request, DomContent...tags){
		var content = div(tags)
				.withClass("container-fluid");
		return factory.startBuilder(request)
				.withTitle(datarouterService.getName())
				.withContent(content)
				.buildMav();
	}

	public Mav homepage(HttpServletRequest request, String containerClass, DomContent...tags){
		var content = div(tags)
				.withClass(containerClass);
		return factory.startBuilder(request)
				.withTitle(datarouterService.getName())
				.withContent(content)
				.buildMav();
	}

	public ContainerTag header(){
		return div(h2(datarouterService.getName()).withClass("text-capitalize"))
				.withClass("pb-2 mt-4 mb-2 border-bottom");
	}

	public ContainerTag headerAndDescription(){
		String description = serviceDescriptionSupplier.get();
		if(description.isBlank()){
			return h2(text(datarouterService.getName()));
		}
		return div(
				h2(datarouterService.getName()).withClass("text-capitalize"),
				h4(description))
				.withClass("pb-2 mt-4 mb-2 border-bottom");
	}

	public ContainerTag docLinks(){
		return ul(each(serviceDocNamesAndLinks.get().entrySet(), entry -> {
			return li(a(entry.getKey()).withHref(entry.getValue()));
		}));
	}

	public ContainerTag appNavbarCards(){
		List<NavBarItem> navBarItems = appNavBarSupplier.get();
		return div(each(NavBarItemGroup.fromNavBarItems(navBarItems), this::makeLinkBox))
				.withClass("container-fluid row");
	}

	public ContainerTag pluginNavbarCards(){
		List<NavBarItem> navBarItems = appPluginNavBarSupplier.get();
		return div(each(NavBarItemGroup.fromNavBarItems(navBarItems), this::makeLinkBox))
				.withClass("container-fluid row");
	}

	private ContainerTag makeLinkBox(NavBarItemGroup group){
		var title = div(group.category.getDisplay())
				.withClass("card-header");
		var body = div(each(group.items, this::makeLink))
				.withClass(" card-body");
		var panel = div(title, body)
				.withClass("card");
		return div(panel)
				.withClass("col-md-4 col-sm-12");
	}

	private ContainerTag makeLink(NavBarItem item){
		return a(item.name)
				.withClass("list-group-item list-group-item-action")
				.withHref(datarouterService.getContextPath() + item.path);
	}

}
