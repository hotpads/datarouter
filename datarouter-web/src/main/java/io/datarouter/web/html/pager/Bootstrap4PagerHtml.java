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
package io.datarouter.web.html.pager;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.span;

import io.datarouter.util.iterable.IterableTool;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.pager.MemoryPager.Page;
import j2html.tags.ContainerTag;

public class Bootstrap4PagerHtml{

	public static ContainerTag renderForm(Page<?> page){
		return div(Bootstrap4FormHtml.render(page.makeHtmlForm(), true));
	}

	public static ContainerTag renderLinkBar(Page<?> page){
		String message = String.format("Showing %s to %s of %s",
				NumberFormatter.addCommas(page.from),
				NumberFormatter.addCommas(page.to),
				NumberFormatter.addCommas(page.totalRows));
		var summary = span(message)
				.withClass("mt-2 ml-2");
		var links = IterableTool.nullSafeMap(page.getLinks(), pageLink -> a(pageLink.text).withHref(pageLink.href));
		var linkSpans = span(each(links, link -> span(link).withClass("ml-2")))
				.withClass("ml-2");
		return div(summary, linkSpans);
	}

}
