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
package io.datarouter.plugin.copytable.web;

import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h3;

import java.util.List;

import io.datarouter.web.html.j2html.J2HtmlTable;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.FormTag;
import j2html.tags.specialized.TableTag;

public class SystemTableCopyHtml{

	public static DivTag makeContent(FormTag formTag, List<String> nodeNames){
		var header = makeHeader(formTag);
		var table = buildTable(nodeNames);
		return div(
				header,
				table)
				.withClass("container-fluid");
	}


	public static TableTag buildTable(List<String> nodeNames){
		return new J2HtmlTable<String>()
				.withClasses("table table-sm table-striped my-4 border")
				.withColumn("", row -> row)
				.build(nodeNames);
	}


	public static DivTag makeHeader(FormTag formTag){
		return div(
				h3("System Table Copier"),
				div("Copy all system tables from one client to another client."),
				br(),
				formTag)
				.withClass("mt-3");
	}

	public static DivTag makeHeader(String titleSuffix, String subtitle){
		return div(makeTitle(titleSuffix, subtitle));
	}

	public static DivTag makeTitle(String titleSuffix, String subtitle){
		return div(
				h3(String.format("%s - %s", "System Table Copier", titleSuffix)),
				div(subtitle))
				.withClass("mt-3");
	}


}
