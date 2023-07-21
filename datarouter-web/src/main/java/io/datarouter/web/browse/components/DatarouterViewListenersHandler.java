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
package io.datarouter.web.browse.components;

import static j2html.TagCreator.div;
import static j2html.TagCreator.h5;

import java.util.List;

import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.listener.AppListenersClasses;
import io.datarouter.web.listener.DatarouterAppListener;
import io.datarouter.web.listener.DatarouterWebAppListener;
import io.datarouter.web.listener.WebAppListenersClasses;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;

public class DatarouterViewListenersHandler extends BaseHandler{

	@Inject
	private AppListenersClasses appListenersClasses;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private WebAppListenersClasses webAppListenersClasses;
	@Inject
	private DatarouterWebPaths paths;

	@Handler(defaultHandler = true)
	public Mav view(){
		var header = DatarouterComponentsHtml.makeHeader(
				paths.datarouter.info.listeners,
				"Listeners",
				"Listeners run some custom code on startup and shutdown");
		var appListeners = makeAppListenersTable(appListenersClasses.getAppListenerClasses());
		var webAppListeners = makeWebAppListenersTable(webAppListenersClasses.getWebAppListenerClasses());
		var content = div(header, appListeners, webAppListeners)
				.withClass("container");
		return pageFactory.startBuilder(request)
				.withTitle("Registered Listeners")
				.withContent(content)
				.buildMav();
	}

	private static DivTag makeAppListenersTable(List<Class<? extends DatarouterAppListener>> rows){
		var tableHeader = h5("App Listeners");
		var table = new J2HtmlTable<Class<? extends DatarouterAppListener>>()
				.withClasses("table table-sm table-striped my-4 border")
				.withColumn("AppListeners", Class::getSimpleName)
				.withCaption("Total " + rows.size())
				.build(rows);
		return div(tableHeader, table)
				.withClass("my-4");
	}

	private static DivTag makeWebAppListenersTable(List<Class<? extends DatarouterWebAppListener>> rows){
		var tableHeader = h5("WebApp Listeners");
		var table = new J2HtmlTable<Class<? extends DatarouterWebAppListener>>()
				.withClasses("table table-sm table-striped my-4 border")
				.withColumn("WebAppListeners", Class::getSimpleName)
				.withCaption("Total " + rows.size())
				.build(rows);
		return div(tableHeader, table)
				.withClass("my-4");
	}

}
