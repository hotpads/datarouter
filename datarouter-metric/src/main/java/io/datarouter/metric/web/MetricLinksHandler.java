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
package io.datarouter.metric.web;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.i;
import static j2html.TagCreator.join;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;

import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.instrumentation.metric.MetricLinkBuilder;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.metriclinks.MetricLinkDto;
import io.datarouter.web.metriclinks.MetricLinkPage;
import io.datarouter.web.metriclinks.MetricLinkPageRegistry;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.TagCreator;
import j2html.tags.ContainerTag;

public class MetricLinksHandler extends BaseHandler{

	@Inject
	private DatarouterInjector injector;
	@Inject
	private MetricLinkPageRegistry registry;
	@Inject
	private MetricLinkPageFactory pageFactory;
	@Inject
	private MetricLinkBuilder linkBuilder;

	@Handler
	public Mav view(){
		List<ContainerTag> tags = Scanner.of(registry.getMetricLinkPages())
				.map(injector::getInstance)
				.sort(Comparator.comparing(MetricLinkPage::getHtmlName))
				.exclude(page -> page.getMetricLinks().isEmpty())
				.map(this::makeContent)
				.list();
		var content = div(each(tags, TagCreator::div));
		return pageFactory.startBuilder(request)
				.withTitle("Metric Links")
				.withContent(content)
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.buildMav();
	}

	private ContainerTag makeContent(MetricLinkPage page){
		var h2 = h2(join(page.getHtmlName(), a(i().withClass("fas fa-home"))
				.withHref("#" + MetricNamesSubnavFactory.ID)))
				.withId(page.getHtmlId());
		List<MetricLinkDto> metricLinks = Scanner.of(page.getMetricLinks())
				.sort(Comparator.comparing(link -> link.name))
				.list();
		var table = new J2HtmlTable<MetricLinkDto>()
				.withClasses("table table-sm table-striped my-4 border")
				.withHtmlColumn(th(page.getName()).withClass("w-50"), row -> td(row.name))
				.withHtmlColumn(th("Exact").withClass("w-25"), row -> {
					if(row.exactMetricName.isEmpty()){
						return td("");
					}
					return td(a(row.exactMetricName.get().display)
							.withHref(linkBuilder.exactMetricLink(row.exactMetricName.get().metric))
							.withTarget("_blank"));
				})
				.withHtmlColumn(th("Available").withClass("w-25"), row -> {
					if(row.availableMetricPrefix.isEmpty()){
						return td("");
					}
					return td(a(row.availableMetricPrefix.get().display)
							.withHref(linkBuilder.availableMetricsLink(row.availableMetricPrefix.get().metric)))
							.withTarget("_blank");
				})
				.withCaption("Total " + metricLinks.size())
				.build(metricLinks);
		return div(h2, table)
				.withClass("container my-4");
	}

}
