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
package io.datarouter.web.html.react;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import io.datarouter.httpclient.path.PathNode;
import io.datarouter.web.config.DatarouterWebFiles;
import io.datarouter.web.css.DatarouterWebCss;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.MavProperties;
import io.datarouter.web.handler.mav.imp.StringMav;
import io.datarouter.web.handler.mav.nav.NavBar;
import io.datarouter.web.js.DatarouterWebJsTool;
import io.datarouter.web.navigation.DatarouterNavbarFactory;
import io.datarouter.web.navigation.DatarouterNavbarHtml;
import io.datarouter.web.navigation.WebappNavbarHtml;
import io.datarouter.web.requirejs.DatarouterWebRequireJs;
import io.datarouter.web.requirejs.RequireJsTool;
import j2html.TagCreator;
import j2html.tags.ContainerTag;
import j2html.tags.EmptyTag;

//bootstrap 3
public class ReactPage{

	private static final DatarouterWebFiles DATAROUTER_WEB_FILES = new DatarouterWebFiles();

	private MavProperties mavProperties;
	private Set<String> require = new TreeSet<>();
	private PathNode reactScript;
	private Map<String,String> jsConstants = new LinkedHashMap<>();
	private String title;

	public ReactHtml build(){
		String contextPath = mavProperties.getContextPath();

		//always include bootstrap
		require.add(DatarouterWebRequireJs.BOOTSTRAP);

		//admin content
		boolean isAdmin = mavProperties.getIsAdmin();
		EmptyTag[] datarouterNavbarCssImports = new EmptyTag[]{};
		ContainerTag datarouterNavbarRequestTimingJsImport = null;
		ContainerTag datarouterNavbar = null;
		ContainerTag datarouterNavbarRequestTimingScript = null;
		if(isAdmin){
			datarouterNavbarCssImports = DatarouterNavbarFactory.makeNavbarCssImportTags(contextPath);
			datarouterNavbarRequestTimingJsImport = DatarouterWebJsTool.makeJsImport(contextPath,
					DATAROUTER_WEB_FILES.js.navbarRequestTimingJs);
			datarouterNavbarRequestTimingScript = DatarouterNavbarFactory.makeNavbarRequestTimingScript(contextPath);
			datarouterNavbar = new DatarouterNavbarHtml(mavProperties).build();
		}

		NavBar navbar = mavProperties.getIsDatarouterPage() ? mavProperties.getDatarouterNavBar()
				: mavProperties.getNavBar();
		return new ReactHtml(
				DatarouterWebCss.makeCssImportTags(contextPath),
				DatarouterWebRequireJs.makeImportTag(contextPath),
				DatarouterWebRequireJs.makeConfigScriptTag(contextPath),
				RequireJsTool.makeRequireScriptTag(require.toArray(String[]::new)),
				datarouterNavbarCssImports,
				datarouterNavbarRequestTimingJsImport,
				datarouterNavbarRequestTimingScript,
				title,
				contextPath + reactScript.toSlashedString(),
				jsConstants,
				datarouterNavbar,
				new WebappNavbarHtml(mavProperties, navbar).build());
	}

	public Mav buildMav(){
		StringBuilder writer = new StringBuilder();
		writer.append(TagCreator.document().render());
		writer.append("\n");
		writer.append(build().build().renderFormatted());
		return new StringMav(writer.toString());
	}

	public ReactPage withMavProperties(MavProperties mavProperties){
		this.mavProperties = mavProperties;
		return this;
	}

	public ReactPage withRequires(String... require){
		this.require.addAll(Arrays.asList(require));
		return this;
	}

	public ReactPage withReactScript(PathNode reactScript){
		this.reactScript = reactScript;
		return this;
	}

	public ReactPage withJsConstant(String key, String value){
		this.jsConstants.put(key, value);
		return this;
	}

	public ReactPage withTitle(String title){
		this.title = title;
		return this;
	}

}
