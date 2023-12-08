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
package io.datarouter.nodewatch.web;

import static j2html.TagCreator.a;
import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h3;
import static j2html.TagCreator.h5;
import static j2html.TagCreator.table;
import static j2html.TagCreator.td;
import static j2html.TagCreator.text;
import static j2html.TagCreator.tr;

import io.datarouter.nodewatch.config.DatarouterNodewatchPlugin;
import j2html.attributes.Attribute;
import j2html.tags.specialized.ATag;
import j2html.tags.specialized.DivTag;

public class NodewatchHtml{

	public static DivTag makeHeader(String titleSuffix, String subtitle){
		return div(makeTitle(titleSuffix, subtitle));
	}

	public static DivTag makeTitle(String titleSuffix, String subtitle){
		return div(
				h3(String.format("%s - %s", DatarouterNodewatchPlugin.NAME, titleSuffix)),
				div(subtitle))
				.withClass("mt-3");
	}

	public static DivTag makeTableInfoDiv(String clientName, String tableName){
		var table = table(
			tr(
				td(text("client"), br(), h5(clientName)).withClass("pr-4"),
				td(text("table"), br(), h5(tableName))));
		return div(table);
	}

	public static ATag makeInfoButton(String name, String href){
		return a(name)
				.withClass("btn btn-info btn-sm ml-1")
				.withHref(href);
	}

	public static ATag makeWarningButton(String name, String href, String confirmationMessage){
		return a(name)
				.withClass("btn btn-warning btn-sm ml-1")
				.withHref(href)
				.attr(makeConfirmAttr(confirmationMessage));
	}

	public static ATag makeDangerButton(String name, String href, String confirmationMessage){
		return a(name)
				.withClass("btn btn-danger btn-sm ml-1")
				.withHref(href)
				.attr(makeConfirmAttr(confirmationMessage));
	}

	public static Attribute makeConfirmAttr(String message){
		String value = String.format("return window.confirm('%s')", message);
		return new Attribute("onclick", value);
	}

}
