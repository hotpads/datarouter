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
import io.datarouter.util.BooleanTool;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.dispatcher.FilterParams;
import io.datarouter.web.dispatcher.FilterParamsSupplier;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;

public class DatarouterViewFiltersHandler extends BaseHandler{

	@Inject
	private FilterParamsSupplier filterParamsSupplier;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterWebPaths paths;

	@Handler(defaultHandler = true)
	public Mav view(){
		var content = makeContent(paths.datarouter.info.filters, filterParamsSupplier.get());
		return pageFactory.startBuilder(request)
				.withTitle("Servlet Filters")
				.withContent(content)
				.buildMav();
	}

	private static DivTag makeContent(PathNode currentPath, List<FilterParams> rows){
		var header = DatarouterComponentsHtml.makeHeader(
				currentPath,
				"Filters",
				"Filters run code before and after each request");
		var table = new J2HtmlTable<FilterParams>()
				.withClasses("table table-sm table-striped my-4 border")
				.withColumn("Filter", row -> row.filterClass.getSimpleName())
				.withColumn("Path", row -> row.path)
				.withColumn("Is Regex", row -> row.isRegex, BooleanTool::toString)
				.withCaption("Total Filters " + rows.size())
				.build(rows);
		return div(header, table)
				.withClass("container");
	}

}
