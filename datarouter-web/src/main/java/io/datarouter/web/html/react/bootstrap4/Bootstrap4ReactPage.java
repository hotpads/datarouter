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
package io.datarouter.web.html.react.bootstrap4;

import static j2html.TagCreator.document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import io.datarouter.pathnode.PathNode;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.config.DatarouterWebFiles;
import io.datarouter.web.css.DatarouterWebCssTool;
import io.datarouter.web.css.DatarouterWebCssV2;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.MavProperties;
import io.datarouter.web.handler.mav.imp.HtmlMav;
import io.datarouter.web.html.CloudflareImports;
import io.datarouter.web.html.react.ReactHtml;
import io.datarouter.web.js.DatarouterWebJsTool;
import io.datarouter.web.navigation.DatarouterNavbarFactory;
import io.datarouter.web.navigation.DatarouterNavbarV2Html;
import io.datarouter.web.navigation.NavBar;
import io.datarouter.web.navigation.WebappNavbarV2Html;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import io.datarouter.web.requirejs.RequireJsTool;
import j2html.tags.ContainerTag;
import j2html.tags.EmptyTag;
import j2html.tags.specialized.ScriptTag;

public class Bootstrap4ReactPage{

	private static final DatarouterWebFiles DATAROUTER_WEB_FILES = new DatarouterWebFiles();

	private MavProperties mavProperties;
	private Set<String> require = new TreeSet<>();
	private PathNode reactScript;
	private List<String> externalJsLibraries = new ArrayList<>();
	private Map<String,String> jsStringConstants = new LinkedHashMap<>();
	private Map<String,String> jsRawConstants = new LinkedHashMap<>();
	private List<PathNode> externalCss = new ArrayList<>();

	private String title;

	public ReactHtml build(){
		String contextPath = mavProperties.getContextPath();

		//always include bootstrap
		require.add(DatarouterWebRequireJsV2.BOOTSTRAP);

		//admin content
		boolean hasAnyDatarouterPrivileges = mavProperties.getHasAnyDatarouterPrivileges();
		EmptyTag<?>[] datarouterNavbarCssImports = new EmptyTag[]{};
		ContainerTag<?> datarouterNavbarRequestTimingJsImport = null;
		ContainerTag<?> datarouterNavbar = null;
		ContainerTag<?> datarouterNavbarRequestTimingScript = null;
		if(hasAnyDatarouterPrivileges){
			// Show the common navbar, which links to the datarouter pages
			int numWebapps = mavProperties.getTomcatWebApps().size();
			datarouterNavbarCssImports = DatarouterNavbarFactory.makeNavbarV2CssImportTags(contextPath, numWebapps);
			datarouterNavbarRequestTimingJsImport = DatarouterWebJsTool.makeJsImport(contextPath,
					DATAROUTER_WEB_FILES.js.navbarRequestTimingV2Js);
			datarouterNavbarRequestTimingScript = DatarouterNavbarFactory.makeNavbarRequestTimingScriptV2(contextPath);
			datarouterNavbar = new DatarouterNavbarV2Html(mavProperties).build();
		}

		NavBar navbar = mavProperties.getIsDatarouterPage() ? mavProperties.getDatarouterNavBar()
				: mavProperties.getNavBar();

		var allScripts = mavProperties.getPageScripts().scripts();

		EmptyTag<?>[] additionalCssImports = DatarouterWebCssTool.makeCssImportTags(contextPath, externalCss);
		return new ReactHtml(
				Scanner.concat(CloudflareImports.REACT_GROUP_2, externalJsLibraries).list(),
				DatarouterWebCssV2.makeCssImportTags(contextPath),
				DatarouterWebRequireJsV2.makeImportTag(contextPath),
				DatarouterWebRequireJsV2.makeConfigScriptTag(contextPath),
				RequireJsTool.makeRequireScriptTag(require.toArray(String[]::new)),
				datarouterNavbarCssImports,
				datarouterNavbarRequestTimingJsImport,
				datarouterNavbarRequestTimingScript,
				title,
				contextPath + reactScript.toSlashedString(),
				jsStringConstants,
				jsRawConstants,
				datarouterNavbar,
				new WebappNavbarV2Html(mavProperties, navbar).build(),
				additionalCssImports,
				allScripts.toArray(ScriptTag[]::new));
	}

	public Mav buildMav(){
		StringBuilder writer = new StringBuilder();
		writer.append(document().render());
		writer.append("\n");
		writer.append(build().build().renderFormatted());
		return new HtmlMav(writer.toString());
	}

	public Bootstrap4ReactPage withMavProperties(MavProperties mavProperties){
		this.mavProperties = mavProperties;
		return this;
	}

	public Bootstrap4ReactPage withRequires(String... require){
		this.require.addAll(Arrays.asList(require));
		return this;
	}

	public Bootstrap4ReactPage withReactScript(PathNode reactScript){
		this.reactScript = reactScript;
		return this;
	}

	public Bootstrap4ReactPage withCss(PathNode externalCss){
		this.externalCss.add(externalCss);
		return this;
	}

	public Bootstrap4ReactPage withExternalJsLibrary(String externalJsLibrary){
		this.externalJsLibraries.add(externalJsLibrary);
		return this;
	}

	public Bootstrap4ReactPage withJsStringConstant(String key, String value){
		this.jsStringConstants.put(key, value);
		return this;
	}

	public Bootstrap4ReactPage withJsRawConstant(String key, String value){
		this.jsRawConstants.put(key, value);
		return this;
	}

	public Bootstrap4ReactPage withTitle(String title){
		this.title = title;
		return this;
	}

}
