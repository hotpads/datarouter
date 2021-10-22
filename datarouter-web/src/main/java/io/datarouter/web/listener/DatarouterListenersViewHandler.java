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

import static j2html.TagCreator.div;
import static j2html.TagCreator.h2;

import java.util.List;

import javax.inject.Inject;

import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.ContainerTag;

public class DatarouterListenersViewHandler extends BaseHandler{

	@Inject
	private AppListenersClasses appListenersClasses;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private WebAppListenersClasses webAppListenersClasses;

	@Handler(defaultHandler = true)
	public Mav view(){
		var appListeners = makeAppListenersTable(appListenersClasses.getAppListenerClasses());
		var webAppListeners = makeWebAppListenersTable(webAppListenersClasses.getWebAppListenerClasses());
		var content = div(appListeners, webAppListeners);
		return pageFactory.startBuilder(request)
				.withTitle("Registered Listeners")
				.withContent(content)
				.buildMav();
	}

	private static ContainerTag<?> makeAppListenersTable(List<Class<? extends DatarouterAppListener>> rows){
		var h2 = h2("Registered App Listeners");
		var table = new J2HtmlTable<Class<? extends DatarouterAppListener>>()
				.withClasses("table table-sm table-striped my-4 border")
				.withColumn("AppListeners", Class::getSimpleName)
				.withCaption("Total " + rows.size())
				.build(rows);
		return div(h2, table)
				.withClass("container my-4");
	}

	private static ContainerTag<?> makeWebAppListenersTable(List<Class<? extends DatarouterWebAppListener>> rows){
		var h2 = h2("Registered WebApp Listeners");
		var table = new J2HtmlTable<Class<? extends DatarouterWebAppListener>>()
				.withClasses("table table-sm table-striped my-4 border")
				.withColumn("WebAppListeners", Class::getSimpleName)
				.withCaption("Total " + rows.size())
				.build(rows);
		return div(h2, table)
				.withClass("container my-4");
	}

}
