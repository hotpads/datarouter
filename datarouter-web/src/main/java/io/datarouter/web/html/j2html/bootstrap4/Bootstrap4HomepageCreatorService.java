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
package io.datarouter.web.html.j2html.bootstrap4;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.li;
import static j2html.TagCreator.text;
import static j2html.TagCreator.ul;

import java.net.URI;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.navigation.AppNavBarRegistrySupplier;
import io.datarouter.web.navigation.AppPluginNavBarSupplier;
import io.datarouter.web.navigation.NavBarItem;
import io.datarouter.web.navigation.NavBarItem.NavBarItemGroup;
import io.datarouter.web.service.DocumentationNamesAndLinksSupplier;
import io.datarouter.web.service.ServiceDescriptionSupplier;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.specialized.ATag;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.UlTag;

@Singleton
public class Bootstrap4HomepageCreatorService{

	@Inject
	private ServiceName serviceName;
	@Inject
	private ServletContextSupplier servletContext;
	@Inject
	private Bootstrap4PageFactory factory;
	@Inject
	private ServiceDescriptionSupplier serviceDescriptionSupplier;
	@Inject
	private DocumentationNamesAndLinksSupplier documentationNamesAndLinksSupplier;
	@Inject
	private AppNavBarRegistrySupplier appNavBarSupplier;
	@Inject
	private AppPluginNavBarSupplier appPluginNavBarSupplier;

	public Mav homepage(HttpServletRequest request, DomContent...tags){
		var content = div(tags)
				.withClass("container-fluid");
		return factory.startBuilder(request)
				.withTitle(serviceName.get())
				.withContent(content)
				.buildMav();
	}

	public Mav homepage(HttpServletRequest request, String containerClass, DomContent...tags){
		var content = div(tags)
				.withClass(containerClass);
		return factory.startBuilder(request)
				.withTitle(serviceName.get())
				.withContent(content)
				.buildMav();
	}

	public DivTag header(){
		return div(h2(serviceName.get()).withClass("text-capitalize"))
				.withClass("pb-2 mt-4 mb-2 border-bottom");
	}

	public ContainerTag<?> headerAndDescription(){
		String description = serviceDescriptionSupplier.get();
		if(description.isBlank()){
			return h2(text(serviceName.get()));
		}
		return div(
				h2(serviceName.get()).withClass("text-capitalize"),
				h4(description))
				.withClass("pb-2 mt-4 mb-2 border-bottom");
	}

	public UlTag docLinks(){
		return ul(each(documentationNamesAndLinksSupplier.getReadmeDocs().entrySet(),
				entry -> li(a(entry.getKey()).withHref(entry.getValue()))));
	}

	public DivTag appNavbarCards(){
		List<NavBarItem> navBarItems = appNavBarSupplier.get();
		return div(each(NavBarItemGroup.fromNavBarItems(navBarItems), this::makeLinkBox))
				.withClass("container-fluid row");
	}

	public DivTag pluginNavbarCards(){
		List<NavBarItem> navBarItems = appPluginNavBarSupplier.get();
		return div(each(NavBarItemGroup.fromNavBarItems(navBarItems), this::makeLinkBox))
				.withClass("container-fluid row");
	}

	private DivTag makeLinkBox(NavBarItemGroup group){
		var title = div(group.category.display())
				.withClass("card-header");
		var body = div(each(group.items, this::makeLink))
				.withClass(" card-body");
		var panel = div(title, body)
				.withClass("card");
		return div(panel)
				.withClass("col-md-4 col-sm-12");
	}

	private ATag makeLink(NavBarItem item){
		String href = URI.create(item.path).isAbsolute() ? item.path : servletContext.getContextPath() + item.path;
		return a(item.name)
				.withClass("list-group-item list-group-item-action")
				.withHref(href);
	}

}
