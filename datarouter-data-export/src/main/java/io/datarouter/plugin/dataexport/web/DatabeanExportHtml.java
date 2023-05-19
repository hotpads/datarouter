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
package io.datarouter.plugin.dataexport.web;

import static j2html.TagCreator.a;
import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h3;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.text;
import static j2html.TagCreator.ul;

import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.client.utils.URIBuilder;

import io.datarouter.pathnode.PathNode;
import io.datarouter.plugin.dataexport.config.DatarouterDataExportPaths;
import io.datarouter.plugin.dataexport.config.DatarouterDataExportPaths.DatabeanExportPaths;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4NavTabsHtml;
import io.datarouter.web.html.nav.NavTabs;
import io.datarouter.web.html.nav.NavTabs.NavTab;
import j2html.TagCreator;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.FormTag;
import j2html.tags.specialized.UlTag;

public class DatabeanExportHtml{

	public static DivTag makeExportContent(PathNode currentPath, HtmlForm htmlForm){
		return div(
				makeHeader(),
				makeExportNav(currentPath),
				makeExportForm(htmlForm))
				.withClass("container mt-3");
	}

	private static DivTag makeHeader(){
		return div(
				h3("Databean Export"),
				div("Export all or part of a table to cloud storage, then import on your localhost"),
				br())
				.withClass("container mt-3");
	}

	private static UlTag makeExportNav(PathNode currentPath){
		DatabeanExportPaths parentPath = new DatarouterDataExportPaths().datarouter.dataExport.exportDatabeans;
		var navTabs = new NavTabs()
				.add(new NavTab(
						"Single Table",
						parentPath.singleTable.getValue(),
						currentPath.equals(parentPath.singleTable)))
				.add(new NavTab(
						"Multi Table",
						parentPath.multiTable.getValue(),
						currentPath.equals(parentPath.multiTable)));
		return Bootstrap4NavTabsHtml.render(navTabs);
	}

	private static FormTag makeExportForm(HtmlForm htmlForm){
		return Bootstrap4FormHtml.render(htmlForm)
				.withClass("card card-body bg-light");
	}

	public static DivTag makeExportCompleteContent(
			String path,
			String exportId,
			List<String> nodeNames,
			long numRows){
		String localImportHref = makeLocalImportHref(path, exportId);
		return div(
				h4("Databean Export Complete"),
				div("exportId: " + exportId),
				div("totalDatabeans: " + NumberFormatter.addCommas(numRows)),
				div(
					div("nodes"),
					makeUl(nodeNames)),
				div(text("Import to localhost by clicking "), a("here").withHref(localImportHref)))
				.withClass("container mt-3");
	}

	private static String makeLocalImportHref(
			String path,
			String exportId){
		try{
			return new URIBuilder()
					.setScheme("https")
					.setHost("localhost")
					.setPort(8443)
					.setPath(path)
					.addParameter(DatarouterDatabeanImportHandler.P_exportId, exportId)
					.build()
					.toString();
		}catch(URISyntaxException e){
			throw new RuntimeException(e);
		}
	}

	public static DivTag makeImportCompleteContent(String exportId, List<PathbeanKey> keys, long totalDatabeans){
		List<String> nodeNames = Scanner.of(keys)
				.map(PathbeanKey::getFile)
				.list();
		return div(
				h4("Databean Import Complete"),
				div("exportId: " + exportId),
				div(
					div("nodes"),
					makeUl(nodeNames)),
				div("totalDatabeans: " + NumberFormatter.addCommas(totalDatabeans)))
				.withClass("container mt-3");
	}

	private static UlTag makeUl(List<String> items){
		var ul = ul();
		Scanner.of(items)
				.map(TagCreator::li)
				.forEach(ul::with);
		return ul;
	}

}
