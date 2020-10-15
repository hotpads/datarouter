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
package io.datarouter.web.navigation;

import static j2html.TagCreator.rawHtml;
import static j2html.TagCreator.script;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import io.datarouter.pathnode.PathNode;
import io.datarouter.web.config.DatarouterWebFiles;
import io.datarouter.web.css.DatarouterWebCssTool;
import io.datarouter.web.handler.mav.MavProperties;
import io.datarouter.web.handler.mav.MavPropertiesFactory;
import io.datarouter.web.html.j2html.J2HtmlTool;
import io.datarouter.web.js.DatarouterWebJsTool;
import j2html.tags.ContainerTag;
import j2html.tags.EmptyTag;

@Singleton
public class DatarouterNavbarFactory{

	private static final DatarouterWebFiles DATAROUTER_WEB_FILES = new DatarouterWebFiles();

	@Inject
	private MavPropertiesFactory mavPropertiesFactory;

	/*----------- v1 common-navbar-b3.jsp --------------*/

	//called by common-navbar-b3.jsp
	public String buildCommonNavbar(HttpServletRequest request){
		MavProperties mavProperties = mavPropertiesFactory.getExistingOrNew(request);
		NavBar navbar = mavProperties.getIsDatarouterPage()
				? mavProperties.getDatarouterNavBar()
				: mavProperties.getNavBar();
		List<String> fragments = new ArrayList<>();
		if(mavProperties.getIsAdmin()){
			fragments.add(makeNavbarCssImportTagsRendered(request.getContextPath()));
			fragments.add(new DatarouterNavbarHtml(mavProperties).build().renderFormatted());
			fragments.add(makeNavbarRequestTiming(request.getContextPath()));
		}
		if(navbar != null){
			fragments.add(new WebappNavbarHtml(mavProperties, navbar).build().renderFormatted());
		}
		return String.join("\n", fragments);
	}

	private static String makeNavbarCssImportTagsRendered(String contextPath){
		EmptyTag[] tags = makeNavbarCssImportTags(contextPath);
		return J2HtmlTool.renderWithLineBreaks(tags);
	}

	public static EmptyTag[] makeNavbarCssImportTags(String contextPath){
		return DatarouterWebCssTool.makeCssImportTags(contextPath, Arrays.asList(
				DATAROUTER_WEB_FILES.css.navbar.navbarCss));
	}

	private static String makeNavbarRequestTiming(String contextPath){
		return J2HtmlTool.renderWithLineBreaks(
				DatarouterWebJsTool.makeJsImport(contextPath, new DatarouterWebFiles().js.navbarRequestTimingJs),
				makeNavbarRequestTimingScript(contextPath));
	}

	public static ContainerTag makeNavbarRequestTimingScript(String contextPath){
		String rawHtml = String.format("addNavbarRequestTiming('%s')", contextPath);
		return script(rawHtml(rawHtml));
	}

	/*----------- v2 common-navbar-b4.jsp --------------*/

	//called by common-navbar-b4.jsp
	public String buildNewCommonNavbar(HttpServletRequest request){
		MavProperties mavProperties = mavPropertiesFactory.getExistingOrNew(request);
		NavBar navbar = mavProperties.getIsDatarouterPage() ? mavProperties.getDatarouterNavBar()
				: mavProperties.getNavBar();
		List<String> fragments = new ArrayList<>();
		if(mavProperties.getIsAdmin()){
			fragments.add(makeNavbarV2CssImportTagsRendered(mavProperties.getContextPath(), mavProperties
					.getTomcatWebApps().size()));
			fragments.add(new DatarouterNavbarV2Html(mavProperties).build().renderFormatted());
			fragments.add(makeNavbarRequestTimingV2(request.getContextPath()));
		}
		if(navbar != null){
			fragments.add(new WebappNavbarV2Html(mavProperties, navbar).build().renderFormatted());
		}
		return String.join("\n", fragments);
	}

	public static String makeNavbarV2CssImportTagsRendered(String contextPath, int numWebapps){
		EmptyTag[] tags = makeNavbarV2CssImportTags(contextPath, numWebapps);
		return J2HtmlTool.renderWithLineBreaks(tags);
	}

	public static EmptyTag[] makeNavbarV2CssImportTags(String contextPath, int numWebapps){
		PathNode mainCss = DATAROUTER_WEB_FILES.css.navbar.navbarV2Css;
		PathNode multiOrSingleWebappCss = numWebapps > 1
				? DATAROUTER_WEB_FILES.css.navbar.navbarV2MultiWebappCss
				: DATAROUTER_WEB_FILES.css.navbar.navbarV2SingleWebappCss;
		return DatarouterWebCssTool.makeCssImportTags(contextPath, Arrays.asList(mainCss, multiOrSingleWebappCss));
	}

	private static String makeNavbarRequestTimingV2(String contextPath){
		return J2HtmlTool.renderWithLineBreaks(
				DatarouterWebJsTool.makeJsImport(contextPath, new DatarouterWebFiles().js.navbarRequestTimingV2Js),
				makeNavbarRequestTimingScriptV2(contextPath));
	}

	public static ContainerTag makeNavbarRequestTimingScriptV2(String contextPath){
		String rawHtml = String.format("addNavbarRequestTiming('%s')", contextPath);
		return script(rawHtml(rawHtml));
	}

}
