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

import java.util.List;

import javax.inject.Inject;

import io.datarouter.pathnode.PathNode;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.config.RouteSetRegistry;
import io.datarouter.web.dispatcher.RouteSet;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.TagCreator;
import j2html.tags.specialized.DivTag;

public class DatarouterViewRouteSetsHandler extends BaseHandler{

	@Inject
	private RouteSetRegistry routeSetRegistry;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterWebPaths paths;

	@Handler(defaultHandler = true)
	public Mav view(){
		var content = makeContent(paths.datarouter.info.routeSets, routeSetRegistry.get());
		return pageFactory.startBuilder(request)
				.withTitle("Registered RouteSets")
				.withContent(content)
				.buildMav();
	}

	private static DivTag makeContent(PathNode path, List<RouteSet> rows){
		var header = DatarouterComponentsHtml.makeHeader(
				path,
				"RouteSets",
				"RouteSets map request paths to Handler classes and can include other config like access controls");
		var table = new J2HtmlTable<RouteSet>()
				.withClasses("table table-sm table-striped my-4 border")
				.withColumn("Route Set Class", row -> row.getClass().getSimpleName())
				.withCaption("Total " + rows.size())
				.build(rows);
		return TagCreator.div(header, table)
				.withClass("container");
	}

}
