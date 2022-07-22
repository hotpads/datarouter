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

import static j2html.TagCreator.document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.MavProperties;
import io.datarouter.web.handler.mav.imp.HtmlMav;
import io.datarouter.web.html.j2html.DatarouterPage;
import io.datarouter.web.html.j2html.DatarouterPageBody;
import io.datarouter.web.navigation.DatarouterNavbarV2Html;
import io.datarouter.web.navigation.NavBar;
import io.datarouter.web.navigation.WebappNavbarV2Html;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.specialized.NavTag;
import j2html.tags.specialized.ScriptTag;

public class Bootstrap4PageBuilder{

	private MavProperties mavProperties;
	private String webappRequireJsConfigJsonString;
	private Set<String> require = new TreeSet<>();
	private String title;
	private boolean includeNav = true;
	private List<NavTag> navbars = new ArrayList<>();
	private DomContent content;
	private final Map<String,String> httpEquivs = new LinkedHashMap<>();
	private final List<ScriptTag> scripts = new ArrayList<>();

	public DatarouterPage build(){
		require.add(DatarouterWebRequireJsV2.BOOTSTRAP);
		boolean isAdmin = mavProperties.getIsAdmin();
		var head = new Bootstrap4PageHead(
				mavProperties,
				mavProperties.getContextPath(),
				webappRequireJsConfigJsonString,
				require.toArray(String[]::new),
				isAdmin,
				title,
				httpEquivs,
				scripts.toArray(ScriptTag[]::new));
		NavBar navbar = mavProperties.getIsDatarouterPage()
				? mavProperties.getDatarouterNavBar()
				: mavProperties.getNavBar();
		List<NavTag> allNavbars = new ArrayList<>();
		if(includeNav){
			if(isAdmin){
				allNavbars.add(new DatarouterNavbarV2Html(mavProperties).build());
			}
			allNavbars.add(new WebappNavbarV2Html(mavProperties, navbar).build());
			allNavbars.addAll(navbars);
		}
		var body = new DatarouterPageBody(allNavbars.toArray(ContainerTag[]::new), content);
		return new DatarouterPage(head, body);
	}

	public Mav buildMav(){
		StringBuilder writer = new StringBuilder();
		writer.append(document().render());
		writer.append("\n");
		writer.append(build().build().renderFormatted());
		return new HtmlMav(writer.toString());
	}

	public Bootstrap4PageBuilder withMavProperties(MavProperties mavProperties){
		this.mavProperties = mavProperties;
		return this;
	}

	public Bootstrap4PageBuilder withWebappRequireJsConfig(String webappRequireJsConfigJsonString){
		this.webappRequireJsConfigJsonString = webappRequireJsConfigJsonString;
		return this;
	}

	public Bootstrap4PageBuilder withRequires(String... require){
		this.require.addAll(Arrays.asList(require));
		return this;
	}

	public Bootstrap4PageBuilder withTitle(String title){
		this.title = title;
		return this;
	}

	public Bootstrap4PageBuilder withHttpEquiv(String httpEquiv, String content){
		httpEquivs.put(httpEquiv, content);
		return this;
	}

	public Bootstrap4PageBuilder includeNav(boolean includeNav){
		this.includeNav = includeNav;
		return this;
	}

	public Bootstrap4PageBuilder withNavbar(NavTag navbar){
		navbars.add(navbar);
		return this;
	}

	public Bootstrap4PageBuilder withContent(DomContent content){
		this.content = content;
		return this;
	}

	public Bootstrap4PageBuilder withScript(ScriptTag script){
		scripts.add(script);
		return this;
	}

}
