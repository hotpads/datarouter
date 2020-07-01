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
package io.datarouter.autoconfig.web;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.td;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.datarouter.autoconfig.config.DatarouterAutoConfigPaths;
import io.datarouter.autoconfig.service.AutoConfigRegistry;
import io.datarouter.inject.DatarouterInjector;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.encoder.RawStringEncoder;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.TagCreator;
import j2html.tags.ContainerTag;

public class ViewAutoConfigsHandler extends BaseHandler{

	private static final String P_name = "name";

	private final Bootstrap4PageFactory pageFactory;
	private final ServerTypeDetector serverTypeDetector;
	private final DatarouterInjector injector;
	private final DatarouterAutoConfigPaths paths;

	private final Map<String,Class<? extends Callable<String>>> map;

	@Inject
	public ViewAutoConfigsHandler(
			Bootstrap4PageFactory pageFactory,
			ServerTypeDetector serverTypeDetector,
			AutoConfigRegistry autoConfigRegistry,
			DatarouterInjector injector,
			DatarouterAutoConfigPaths paths){
		this.pageFactory = pageFactory;
		this.serverTypeDetector = serverTypeDetector;
		this.injector = injector;
		this.paths = paths;
		map = new HashMap<>();

		Scanner.of(autoConfigRegistry.autoConfigs)
				.map(injector::getInstance)
				.forEach(config -> map.put(config.getName(), config.getClass()));
		Scanner.of(autoConfigRegistry.autoConfigGroups)
				.map(injector::getInstance)
				.forEach(config -> map.put(config.getName(), config.getClass()));
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
		var content = div(header, makeContent(map))
				.withClass("container-fluid");
		return pageFactory.startBuilder(request)
				.withTitle("Registered AutoConfig Classes")
				.withContent(content)
				.buildMav();
	}

	@Handler(encoder = RawStringEncoder.class)
	public String runForName(@Param(P_name) String name) throws Exception{
		if(serverTypeDetector.mightBeProduction()){
			return "This is not supported on production";
		}
		Class<? extends Callable<String>> callableClass = map.get(name);
		return injector.getInstance(callableClass).call();
	}

	private ContainerTag makeContent(Map<String,Class<? extends Callable<String>>> map){
		var table = new J2HtmlTable<Entry<String,Class<? extends Callable<String>>>>()
				.withClasses("table table-sm table-striped my-4 border")
				.withColumn("AutoConfig", row -> row.getKey())
				.withHtmlColumn("Run", row ->
						td(a("Trigger")
								.withClass("btn btn-primary")
								.withType("button")
								.withHref(request.getServletContext().getContextPath()
										+ paths.datarouter.autoConfigs.runForName.toSlashedString()
										+ "?" + P_name + "=" + row.getKey())))
				.withCaption("Total: " + map.size())
				.build(map.entrySet());
		return div(table)
				.withClass("container my-4");
	}

}
