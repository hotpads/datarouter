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
package io.datarouter.web.html.indexpager;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.span;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import j2html.tags.specialized.DivTag;

public class Bootstrap4IndexPagerHtml{

	public static DivTag render(IndexPage<?> indexPage, String path){
		return div(
				renderForm(indexPage),
				renderLinkBar(indexPage, path)
						.withClass("mt-2"));
	}

	public static DivTag renderForm(IndexPage<?> indexPage){
		HtmlForm form = IndexPagerHtml.makeForm(indexPage);
		return div(Bootstrap4FormHtml.render(form, true));
	}

	public static DivTag renderLinkBar(IndexPage<?> indexPage, String path){
		String message = String.format("Showing %s to %s",
				NumberFormatter.addCommas(indexPage.fromRow),
				NumberFormatter.addCommas(indexPage.toRow));
		if(indexPage.totalRows.isPresent()){
			message += " of " + NumberFormatter.addCommas(indexPage.totalRows.get());
		}
		var summary = span(message)
				.withClass("mt-2 ml-2");
		var links = Scanner.of(IndexPagerHtml.makeLinks(path, indexPage))
				.map(pageLink -> a(pageLink.text)
						.withHref(pageLink.href))
				.list();
		var linkSpans = span(each(links, link -> span(link).withClass("ml-2")))
				.withClass("ml-2");
		return div(summary, linkSpans);
	}

}
