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
package io.datarouter.web.inspect;

import static j2html.TagCreator.div;

import javax.inject.Inject;

import io.datarouter.storage.config.DatarouterPropertiesService;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.J2HtmlLegendTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.ContainerTag;

public class DatarouterPropertiesViewHandler extends BaseHandler{

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterPropertiesService service;

	@Handler(defaultHandler = true)
	public Mav view(){
		var content = makeTable();
		return pageFactory.startBuilder(request)
				.withTitle("Datarouter Properties")
				.withContent(content)
				.buildMav();
	}

	private ContainerTag<?> makeTable(){
		var table = new J2HtmlLegendTable()
				.withClass("sortable table table-sm border table-striped")
				.withSingleRow(false);
		service.getAllProperties()
				.forEach(row -> table.withEntry(row.getLeft(), row.getRight() == null ? "" : row.getRight()));
		return div(table.build())
				.withClass("container my-4");
	}

}
