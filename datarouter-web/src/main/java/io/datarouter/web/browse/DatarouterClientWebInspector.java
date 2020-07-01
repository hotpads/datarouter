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
package io.datarouter.web.browse;

import static j2html.TagCreator.a;
import static j2html.TagCreator.b;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.li;
import static j2html.TagCreator.nav;
import static j2html.TagCreator.ol;
import static j2html.TagCreator.span;
import static j2html.TagCreator.table;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;
import static j2html.TagCreator.thead;
import static j2html.TagCreator.tr;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.params.Params;
import j2html.tags.ContainerTag;

public interface DatarouterClientWebInspector{

	Mav inspectClient(Params params, HttpServletRequest request);

	default ContainerTag buildClientOptionsTable(Map<String,String> allClientOptions){
		var thead = thead(tr(th("Option Key"), th("Option Value")));
		var table = table()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.with(thead);
		allClientOptions.entrySet().stream()
				.map(entry -> tr(td(entry.getKey()), td(entry.getValue())))
				.forEach(table::with);
		ContainerTag header = h4("Client Options:");
		return div(header, table)
				.withClass("container-fluid my-4");
	}

	static ContainerTag buildNav(String contextPath, String clientName){
		var datarouterHome = a("Datarouter Home")
				.withHref(contextPath + new DatarouterWebPaths().datarouter.toSlashedString());
		var datarouterNavItem = li(datarouterHome)
				.withClass("breadcrumb-item");
		var client = span(clientName)
				.withClass("breadcrumb-item active");
		var clientNavItem = li(b("Client: "), client)
				.withClass("breadcrumb");
		return nav(ol(datarouterNavItem, clientNavItem));
	}

}
