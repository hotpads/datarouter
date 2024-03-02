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

import java.util.List;

import io.datarouter.pathnode.PathNode;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.plugin.PluginRegistrySupplier;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;

public class DatarouterViewPluginsHandler extends BaseHandler{

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private PluginRegistrySupplier pluginRegistry;
	@Inject
	private DatarouterWebPaths paths;

	@Handler(defaultHandler = true)
	public Mav view(){
		List<String> plugins = pluginRegistry.get().stream()
				.sorted()
				.toList();
		var content = makeContent(paths.datarouter.info.plugins, plugins);
		return pageFactory.startBuilder(request)
				.withTitle("Registered Plugins")
				.withContent(content)
				.buildMav();
	}

	private static DivTag makeContent(PathNode currentPath, List<String> rows){
		var header = DatarouterComponentsHtml.makeHeader(
				currentPath,
				"Plugins",
				"A plugin bundles a bunch of other compents like RouteSets and TriggerGroups into a single class");
		var table = new J2HtmlTable<String>()
				.withClasses("table table-sm table-striped my-4 border")
				.withColumn("Plugin Name", row -> row)
				.withCaption("Total Plugins " + rows.size())
				.build(rows);
		return div(header, table)
				.withClass("container");
	}

}
