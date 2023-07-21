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
package io.datarouter.web.browse;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.i;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import io.datarouter.web.service.DocumentationNamesAndLinksSupplier;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;

public class ViewReadmesHandler extends BaseHandler{

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DocumentationNamesAndLinksSupplier docNameAndLinksSupplier;

	@Handler(defaultHandler = true)
	public Mav view(){
		var rows = docNameAndLinksSupplier.getReadmeDocs();
		return pageFactory.startBuilder(request)
				.withTitle("Readme Links")
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(makeContent("Readmes", rows))
				.buildMav();
	}

	public static DivTag makeContent(String title, Map<String,String> rows){
		var header = h2(title);
		Set<Entry<String,String>> entrySet = rows.entrySet().stream()
				.sorted(Comparator.comparing(Entry::getKey))
				.collect(Collectors.toSet());
		var table = new J2HtmlTable<Entry<String,String>>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withCaption("Total : " + rows.size())
				.withHtmlColumn(th("Name").withClass("w-50"), row -> td(row.getKey()))
				.withHtmlColumn(th("").withClass("w-25"), row -> {
					return td(a(i().withClass("fa fa-link"))
							.withClass("btn btn-link w-100 py-0")
							.withHref(row.getValue())
							.withTarget("_blank"));
				})
				.build(entrySet);
		return div(header, table)
				.withClass("container my-4");
	}

}
