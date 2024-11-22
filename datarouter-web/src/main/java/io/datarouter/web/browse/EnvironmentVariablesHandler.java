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

import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h3;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;
import static j2html.TagCreator.tr;

import java.util.Map;
import java.util.TreeMap;

import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TdTag;
import jakarta.inject.Inject;

public class EnvironmentVariablesHandler extends BaseHandler{
	private static final String SENSITIVE = "********";

	@Inject
	private Bootstrap4PageFactory pageFactory;

	@Handler
	public Mav envVars(){
		var header = div(
				h3("Environment Variables"),
				div("System environment variables set on the current server."),
				br())
				.withClass("mt-3");
		var table = buildEnvironmentVariablesTable();
		var content = div(header, table)
				.withClass("container");
		return pageFactory.startBuilder(request)
				.withTitle("Environment Variables")
				.withContent(content)
				.buildMav();
	}

	private DivTag buildEnvironmentVariablesTable(){
		var tbody = tbody();
		Map<String,String> sortedEnvVars = new TreeMap<>(System.getenv());
		sortedEnvVars.forEach((key, value) -> {
			TdTag leftTd = td().withText(key);
			TdTag rightTd = isSensitiveVariable(key) ? td().withText(SENSITIVE) : td().withText(value);
			tbody.with(tr(leftTd, rightTd));
		});

		var table = table(
				th().withScope("col").withText("Key"),
				th().withScope("col").withText("Value"),
				tbody)
				.withClass("table table-striped table-bordered table-sm table-sortable");

		return div(table);
	}

	private boolean isSensitiveVariable(String value){
		return value.toLowerCase().contains("secret")
				|| value.toLowerCase().contains("password");
	}

}
