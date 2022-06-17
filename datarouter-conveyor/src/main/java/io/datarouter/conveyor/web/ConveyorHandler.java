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
package io.datarouter.conveyor.web;

import static j2html.TagCreator.div;
import static j2html.TagCreator.h4;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.datarouter.conveyor.BaseConveyors;
import io.datarouter.conveyor.dto.ConveyorSummary;
import io.datarouter.inject.DatarouterInjector;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.tags.specialized.DivTag;

public class ConveyorHandler extends BaseHandler{

	@Inject
	private DatarouterInjector injector;
	@Inject
	private Bootstrap4PageFactory pageFactory;

	@Handler
	private Mav list(){
		Map<String,BaseConveyors> allBaseConveyors = injector.getInstancesOfType(BaseConveyors.class);
		List<ConveyorSummary> collect = allBaseConveyors.values().stream()
				.map(BaseConveyors::getExecsAndConveyorsbyName)
				.map(ConveyorSummary::summarize)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
		return pageFactory.startBuilder(request)
				.withTitle("Conveyors")
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(makeContent(collect))
				.buildMav();
	}

	private DivTag makeContent(Collection<ConveyorSummary> rows){
		var title = h4("Conveyors")
				.withClass("mt-2");
		var table = new J2HtmlTable<ConveyorSummary>()
				.withClasses("sortable table table-sm table-striped border")
				.withColumn("Name", row -> row.name)
				.build(rows);
		return div(title, table)
				.withClass("container-fluid");
	}

}
