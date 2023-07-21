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
package io.datarouter.autoconfig.web;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.td;

import java.util.Map.Entry;
import java.util.concurrent.Callable;

import io.datarouter.autoconfig.config.DatarouterAutoConfigPaths;
import io.datarouter.autoconfig.service.AutoConfigService;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.TagCreator;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;

public class ViewAutoConfigsHandler extends BaseHandler{

	private final Bootstrap4PageFactory pageFactory;
	private final ServerTypeDetector serverTypeDetector;
	private final AutoConfigService autoConfigService;
	private final DatarouterAutoConfigPaths paths;

	@Inject
	public ViewAutoConfigsHandler(
			Bootstrap4PageFactory pageFactory,
			ServerTypeDetector serverTypeDetector,
			AutoConfigService autoConfigService,
			DatarouterAutoConfigPaths paths){
		this.pageFactory = pageFactory;
		this.serverTypeDetector = serverTypeDetector;
		this.autoConfigService = autoConfigService;
		this.paths = paths;
	}

	@Handler
	public Mav viewAutoConfigs(){
		if(serverTypeDetector.mightBeProduction()){
			return pageFactory.message(request, "This is not supported on production");
		}
		var runAllButton = a("Run All")
				.withClass("btn btn-primary")
				.withType("button")
				.withHref(request.getServletContext().getContextPath() + paths.datarouter.autoConfig.toSlashedString());
		var header = div(TagCreator.h2("AutoConfigs"), runAllButton);
		var content = div(header, makeContent())
				.withClass("container-fluid");
		return pageFactory.startBuilder(request)
				.withTitle("Registered AutoConfig Classes")
				.withContent(content)
				.buildMav();
	}

	private DivTag makeContent(){
		var table = new J2HtmlTable<Entry<String,Callable<String>>>()
				.withClasses("table table-sm table-striped my-4 border")
				.withColumn("AutoConfig", Entry::getKey)
				.withHtmlColumn("Run", row ->
						td(a("Trigger")
								.withClass("btn btn-primary")
								.withType("button")
								.withHref(request.getServletContext().getContextPath()
										+ paths.datarouter.autoConfigs.runForName.toSlashedString()
										+ "?" + DatarouterAutoConfigHandler.P_name + "=" + row.getKey())))
				.withCaption("Total: " + autoConfigService.getAutoConfigByName().size())
				.build(autoConfigService.getAutoConfigByName().entrySet());
		return div(table)
				.withClass("container my-4");
	}

}
