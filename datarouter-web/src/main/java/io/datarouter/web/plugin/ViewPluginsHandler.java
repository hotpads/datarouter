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
package io.datarouter.web.plugin;

import static j2html.TagCreator.div;
import static j2html.TagCreator.h2;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.ContainerTag;

public class ViewPluginsHandler extends BaseHandler{

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private PluginRegistrySupplier pluginRegistry;

	@Handler(defaultHandler = true)
	public Mav view(){
		List<String> plugins = pluginRegistry.get().stream()
				.sorted()
				.collect(Collectors.toList());
		var content = makeContent(plugins);
		return pageFactory.startBuilder(request)
				.withTitle("Registered Plugins")
				.withContent(content)
				.buildMav();
	}

	private static ContainerTag makeContent(List<String> rows){
		var h2 = h2("Registered Plugins");
		var table = new J2HtmlTable<String>()
				.withClasses("table table-sm table-striped my-4 border")
				.withColumn("Plugin Name", row -> row)
				.withCaption("Total Plugins " + rows.size())
				.build(rows);
		return div(h2, table)
				.withClass("container my-4");
	}

}
