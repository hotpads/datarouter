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
package io.datarouter.web.listener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.web.css.DatarouterWebCss;
import io.datarouter.web.css.DatarouterWebCssV2;
import io.datarouter.web.handler.mav.MavPropertiesFactory;
import io.datarouter.web.html.j2html.J2HtmlTool;
import io.datarouter.web.navigation.DatarouterNavbarFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJs;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import io.datarouter.web.requirejs.RequireJsTool;

@Singleton
public class JspWebappListener extends DatarouterWebAppListener{

	private static final List<String> ATTRIBUTES = new ArrayList<>();

	protected static final String MAV_PROPERTIES_FACTORY = register("mavPropertiesFactory");
	protected static final String CSS_IMPORT_CONTENT = register("datarouterCssImportContent");
	protected static final String NEW_CSS_IMPORT_CONTENT = register("datarouterNewCssImportContent");
	protected static final String BASE_HEAD_CONTENT = register("datarouterBaseHeadContent");
	protected static final String NEW_BASE_HEAD_CONTENT = register("datarouterNewBaseHeadContent");
	protected static final String NAVBAR_FACTORY = register("datarouterNavbarFactory");

	@Inject
	private MavPropertiesFactory mavPropertiesFactory;
	@Inject
	private DatarouterNavbarFactory datarouterNavbarFactory;

	@Override
	public void onStartUp(){
		String contextPath = servletContext.getContextPath();
		servletContext.setAttribute(MAV_PROPERTIES_FACTORY, mavPropertiesFactory);
		servletContext.setAttribute(CSS_IMPORT_CONTENT, makeCssImportContent(contextPath));
		servletContext.setAttribute(NEW_CSS_IMPORT_CONTENT, makeNewCssImportContent(contextPath));
		servletContext.setAttribute(BASE_HEAD_CONTENT, makeBaseHeadContent(contextPath));
		servletContext.setAttribute(NEW_BASE_HEAD_CONTENT, makeNewBaseHeadContent(contextPath));
		servletContext.setAttribute(NAVBAR_FACTORY, datarouterNavbarFactory);
	}

	@Override
	public void onShutDown(){
		ATTRIBUTES.forEach(servletContext::removeAttribute);
	}

	private static String register(String attribute){
		ATTRIBUTES.add(attribute);
		return attribute;
	}

	private static String makeCssImportContent(String contextPath){
		return J2HtmlTool.renderWithLineBreaks(DatarouterWebCss.makeCssImportTags(contextPath));
	}

	private static String makeNewCssImportContent(String contextPath){
		return J2HtmlTool.renderWithLineBreaks(DatarouterWebCssV2.makeCssImportTags(contextPath));
	}

	private static String makeBaseHeadContent(String contextPath){
		return J2HtmlTool.renderWithLineBreaks(
				DatarouterWebRequireJs.makeImportTag(contextPath),
				DatarouterWebRequireJs.makeConfigScriptTag(contextPath),
				RequireJsTool.makeRequireScriptTag(DatarouterWebRequireJs.BOOTSTRAP));
	}

	private static String makeNewBaseHeadContent(String contextPath){
		return J2HtmlTool.renderWithLineBreaks(
				DatarouterWebRequireJs.makeImportTag(contextPath),
				DatarouterWebRequireJsV2.makeConfigScriptTag(contextPath),
				RequireJsTool.makeRequireScriptTag(DatarouterWebRequireJsV2.BOOTSTRAP));
	}

}
