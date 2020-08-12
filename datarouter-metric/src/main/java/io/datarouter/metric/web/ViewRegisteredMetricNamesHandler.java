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
import static j2html.TagCreator.h2;
import static j2html.TagCreator.i;
import static j2html.TagCreator.td;

import java.util.List;

import javax.inject.Inject;

import io.datarouter.metric.MetricLinkBuilder;
import io.datarouter.metric.MetricName;
import io.datarouter.metric.MetricNameRegistry;
import io.datarouter.metric.MetricNameType;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;

public class ViewRegisteredMetricNamesHandler extends BaseHandler{

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private MetricNameRegistry registry;
	@Inject
	private MetricLinkBuilder linkBuilder;

	@Handler(defaultHandler = true)
	public Mav view(){
		List<MetricName> names = registry.metricNames;
		var content = makeContent(names);
		return pageFactory.startBuilder(request)
				.withTitle("Registered Metric Names")
				.withContent(content)
				.buildMav();
	}

	private ContainerTag makeContent(List<MetricName> rows){
		var h2 = h2("Registered Metric Names");
		var table = new J2HtmlTable<MetricName>()
				.withClasses("table table-sm table-striped my-4 border")
				.withColumn("Metric Name", row -> row.name)
				.withColumn("Type", row -> row.nameType.type)
				.withHtmlColumn("", this::getLink)
				.build(rows);
		return div(h2, table)
				.withClass("container my-4");
	}

	public DomContent getLink(MetricName metricName){
		String link;
		if(metricName.nameType == MetricNameType.AVAILABLE){
			link = linkBuilder.availableMetricsLink(metricName.name);
		}else{
			link = linkBuilder.exactMetricLink(metricName.name, metricName.metricType.type);
		}
		return td(a(i().withClass("fa fa-link"))
				.withClass("btn btn-link w-100 py-0")
				.withHref(link));
	}

}
