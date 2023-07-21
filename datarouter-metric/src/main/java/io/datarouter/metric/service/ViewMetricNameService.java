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
package io.datarouter.metric.service;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.i;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;

import java.util.Comparator;
import java.util.List;

import io.datarouter.instrumentation.metric.MetricLinkBuilder;
import io.datarouter.metric.dto.MetricDashboardDto;
import io.datarouter.metric.dto.MetricName;
import io.datarouter.metric.dto.MiscMetricLinksDto;
import io.datarouter.metric.links.MetricDashboardRegistry;
import io.datarouter.metric.links.MiscMetricsLinksRegistry;
import io.datarouter.metric.types.MetricNameType;
import io.datarouter.web.html.j2html.J2HtmlTable;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TdTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ViewMetricNameService{

	@Inject
	private MetricLinkBuilder linkBuilder;
	@Inject
	private MetricDashboardRegistry dashboardRegistry;
	@Inject
	private MiscMetricsLinksRegistry miscMetricLinksRegistry;

	public DivTag makeMetricNameTable(String header, List<MetricName> rows){
		if(rows.size() == 0){
			return div();
		}
		var h2 = h2(header);
		rows.sort(Comparator.comparing(metricName -> metricName.displayName));
		var table = new J2HtmlTable<MetricName>()
				.withClasses("table table-sm table-striped my-4 border")
				.withHtmlColumn(th("Metric Name").withClass("w-50"), row -> td(row.displayName))
				.withHtmlColumn(th("Type").withClass("w-25"), row -> td(row.nameType.type))
				.withHtmlColumn(th("").withClass("w-25"), this::getMetricNameLink)
				.withCaption("Total " + rows.size())
				.build(rows);
		return div(h2, table)
				.withClass("container my-4");
	}

	private TdTag getMetricNameLink(MetricName metricName){
		String link = metricName.nameType == MetricNameType.AVAILABLE
				? linkBuilder.availableMetricsLink(metricName.getNameOrPrefix())
				: linkBuilder.exactMetricLink(metricName.getNameOrPrefix());
		return td(a(i().withClass("fa fa-link"))
				.withClass("btn btn-link w-100 py-0")
				.withHref(link)
				.withTarget("_blank"));
	}


	public DivTag getDashboardsTable(){
		var dasboards = dashboardRegistry.dashboards;
		if(dasboards.size() == 0){
			return div();
		}
		var h2 = h2("Metric Dashboards");
		dasboards.sort(Comparator.comparing(metricName -> metricName.displayName));
		var table = new J2HtmlTable<MetricDashboardDto>()
				.withClasses("table table-sm table-striped my-4 border")
				.withHtmlColumn(th("Dashboard Name").withClass("w-50"), row -> td(row.displayName))
				.withHtmlColumn(th("").withClass("w-25"), this::getDashboardLink)
				.build(dasboards);
		return div(h2, table)
				.withClass("container my-4");
	}

	private TdTag getDashboardLink(MetricDashboardDto dashboard){
		String link = linkBuilder.dashboardLink(dashboard.id);
		return td(a(i().withClass("fa fa-link"))
				.withClass("btn btn-link w-100 py-0")
				.withHref(link)
				.withTarget("_blank"));
	}

	public DivTag miscMetricLinksTable(){
		var links = miscMetricLinksRegistry.miscMetricLinks;
		if(links.size() == 0){
			return div();
		}
		var h2 = h2("Misc Metric Links");
		links.sort(Comparator.comparing(dto -> dto.display));
		var table = new J2HtmlTable<MiscMetricLinksDto>()
				.withClasses("table table-sm table-striped my-4 border")
				.withHtmlColumn(th("Metric Name").withClass("w-50"), row -> td(row.display))
				.withHtmlColumn(th("").withClass("w-25"), row -> {
					return td(a(i().withClass("fa fa-link"))
							.withClass("btn btn-link w-100 py-0")
							.withHref(row.link)
							.withTarget("_blank"));
				})
				.build(links);
		return div(h2, table)
				.withClass("container my-4");
	}

}
