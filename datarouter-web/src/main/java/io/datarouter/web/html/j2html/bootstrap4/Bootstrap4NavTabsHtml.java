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
import static j2html.TagCreator.li;
import static j2html.TagCreator.ul;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.scanner.Scanner;
import io.datarouter.web.html.nav.NavTabs;
import io.datarouter.web.html.nav.NavTabs.NavTab;
import j2html.tags.specialized.ATag;
import j2html.tags.specialized.LiTag;
import j2html.tags.specialized.UlTag;

public class Bootstrap4NavTabsHtml{

	public static UlTag render(NavTabs navTabs){
		var nav = ul()
				.withClass("nav nav-tabs");
		Scanner.of(navTabs.navTabs)
				.map(Bootstrap4NavTabsHtml::makeTab)
				.forEach(nav::with);
		return nav;
	}

	private static LiTag makeTab(NavTab tab){
		return li(makeTabAnchor(tab))
				.withClass("nav-item");
	}

	private static ATag makeTabAnchor(NavTab tab){
		List<String> anchorClasses = new ArrayList<>();
		anchorClasses.add("nav-link");
		if(tab.active()){
			anchorClasses.add("active");
		}
		return a(tab.name())
				.withClass(String.join(" ", anchorClasses))
				.withHref(tab.href());
	}

}
