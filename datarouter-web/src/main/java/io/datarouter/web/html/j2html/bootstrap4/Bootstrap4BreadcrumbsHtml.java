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
package io.datarouter.web.html.j2html.bootstrap4;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.li;
import static j2html.TagCreator.ol;
import static j2html.TagCreator.span;
import static j2html.TagCreator.strong;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.scanner.Scanner;
import io.datarouter.web.html.nav.Breadcrumbs;
import io.datarouter.web.html.nav.Breadcrumbs.Breadcrumb;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.LiTag;

public class Bootstrap4BreadcrumbsHtml{

	public static DivTag render(Breadcrumbs breadcrumbs){
		var ol = ol()
				.withClass("breadcrumb");
		Scanner.of(breadcrumbs.breadcrumbs)
				.map(Bootstrap4BreadcrumbsHtml::makeBreadcrumb)
				.forEach(ol::with);
		return div(ol);
	}

	private static LiTag makeBreadcrumb(Breadcrumb breadcrumb){
		List<String> breadcrumbClasses = new ArrayList<>();
		breadcrumbClasses.add("breadcrumb-item");
		var nameSpan = breadcrumb.active() ? strong(breadcrumb.name()) : span(breadcrumb.name());
		var anchor = a(nameSpan)
				.withHref(breadcrumb.href());
		return li(anchor)
				.withClass(String.join(" ", breadcrumbClasses));
	}

}
