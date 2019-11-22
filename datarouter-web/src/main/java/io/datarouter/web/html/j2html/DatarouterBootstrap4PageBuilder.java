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
package io.datarouter.web.html.j2html;

import static j2html.TagCreator.document;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.MavProperties;
import io.datarouter.web.handler.mav.imp.StringMav;
import io.datarouter.web.handler.mav.nav.NavBar;
import io.datarouter.web.navigation.DatarouterNavbarV2Html;
import io.datarouter.web.navigation.WebappNavbarV2Html;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.tags.ContainerTag;

public class DatarouterBootstrap4PageBuilder{

	private MavProperties mavProperties;
	private String webappRequireJsConfigJsonString;
	private Set<String> require = new TreeSet<>();
	private String title;
	private ContainerTag content;

	public DatarouterPage build(){
		require.add(DatarouterWebRequireJsV2.BOOTSTRAP);
		boolean isAdmin = mavProperties.getIsAdmin();
		var head = new DatarouterBootstrap4PageHead(
				mavProperties,
				mavProperties.getContextPath(),
				webappRequireJsConfigJsonString,
				require.toArray(String[]::new),
				isAdmin,
				title);
		NavBar navbar = mavProperties.getIsDatarouterPage()
				? mavProperties.getDatarouterNavBar()
				: mavProperties.getNavBar();
		var datarouterNavbar = isAdmin ? new DatarouterNavbarV2Html(mavProperties).build() : null;
		var webappNavbar = new WebappNavbarV2Html(mavProperties, navbar).build();
		var body = new DatarouterPageBody(datarouterNavbar, webappNavbar, content);
		return new DatarouterPage(head, body);
	}

	public Mav buildMav(){
		StringBuilder writer = new StringBuilder();
		writer.append(document().render());
		writer.append("\n");
		writer.append(build().build().renderFormatted());
		return new StringMav(writer.toString());
	}

	public DatarouterBootstrap4PageBuilder withMavProperties(MavProperties mavProperties){
		this.mavProperties = mavProperties;
		return this;
	}

	public DatarouterBootstrap4PageBuilder withWebappRequireJsConfig(String webappRequireJsConfigJsonString){
		this.webappRequireJsConfigJsonString = webappRequireJsConfigJsonString;
		return this;
	}

	public DatarouterBootstrap4PageBuilder withRequires(String... require){
		this.require.addAll(Arrays.asList(require));
		return this;
	}

	public DatarouterBootstrap4PageBuilder withTitle(String title){
		this.title = title;
		return this;
	}

	public DatarouterBootstrap4PageBuilder withContent(ContainerTag content){
		this.content = content;
		return this;
	}

}
