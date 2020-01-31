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

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.web.navigation.NavBarItem;
import io.datarouter.web.navigation.NavBarItem.NavBarItemGroup;
import j2html.tags.ContainerTag;

@Deprecated // abstract out to j2 builder class
@Singleton
public class Bootstrap4CardService{

	@Inject
	private DatarouterService datarouterService;

	public ContainerTag render(List<NavBarItemGroup> groups){
		return div(each(groups, this::makeLinkBox))
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
