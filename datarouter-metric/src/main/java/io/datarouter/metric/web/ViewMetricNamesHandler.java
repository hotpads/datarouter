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
import static j2html.TagCreator.th;

import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.job.TriggerGroupClasses;
import io.datarouter.job.scheduler.JobPackage;
import io.datarouter.metric.MetricLinkBuilder;
import io.datarouter.metric.MetricName;
import io.datarouter.metric.MetricNameRegistry;
import io.datarouter.metric.MetricNameType;
import io.datarouter.metric.MetricType;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.web.config.RouteSetRegistry;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.optional.OptionalBoolean;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;

public class ViewMetricNamesHandler extends BaseHandler{

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private MetricNameRegistry registry;
	@Inject
	private MetricLinkBuilder linkBuilder;
	@Inject
	private DatarouterClients clients;
	@Inject
	private DatarouterNodes datarouterNodes;
	@Inject
	private DatarouterInjector injector;
	@Inject
	private TriggerGroupClasses triggerGroupClasses;
	@Inject
	private RouteSetRegistry routeSetRegistry;

	@Handler(defaultHandler = true)
	public Mav view(OptionalBoolean showSystemInfo){
		boolean showSystem = showSystemInfo.orElse(false);
		var customNames = makeTable("Custom", registry.metricNames);
		var tables = makeTable("Tables", getNodeTableMetricNames(showSystem));
		var jobs = makeTable("Jobs", getJobMetricNames(showSystem));
		var handlers = makeTable("Handlers", getHandlerMetricNames(showSystem));
		var content = div(customNames, handlers, jobs, tables)
				.withClass("container my-4");
		String title = "Suggested Metric Names - " + (showSystem ? "System" : "App");
		return pageFactory.startBuilder(request)
				.withTitle(title)
				.withContent(content)
				.buildMav();
	}

	private ContainerTag makeTable(String header, List<MetricName> rows){
		if(rows.size() == 0){
			return div();
		}
		var h2 = h2(header);
		rows.sort(Comparator.comparing(metricName -> metricName.displayName));
		var table = new J2HtmlTable<MetricName>()
				.withClasses("table table-sm table-striped my-4 border")
				.withHtmlColumn(th("Metric Name").withClass("w-50"), row -> td(row.displayName))
				.withHtmlColumn(th("Type").withClass("w-25"), row -> td(row.nameType.type))
				.withHtmlColumn(th("").withClass("w-25"), this::getLink)
				.build(rows);
		return div(h2, table);
	}

	private DomContent getLink(MetricName metricName){
		String link;
		if(metricName.nameType == MetricNameType.AVAILABLE){
			link = linkBuilder.availableMetricsLink(metricName.getNameOrPrefix());
		}else{
			link = linkBuilder.exactMetricLink(metricName.getNameOrPrefix(), metricName.metricType.type);
		}
		return td(a(i().withClass("fa fa-link"))
				.withClass("btn btn-link w-100 py-0")
				.withHref(link));
	}

	private List<MetricName> getNodeTableMetricNames(boolean showSystemInfo){
		return Scanner.of(clients.getClientIds())
				.include(ClientId::getWritable)
				.map(ClientId::getName)
				.map(datarouterNodes::getPhysicalNodesForClient)
				.concat(Scanner::of)
				.map(PhysicalNode::getFieldInfo)
				.include(fieldInfo -> {
					if(showSystemInfo){
						return fieldInfo.getIsSystemTable();
					}
					return !fieldInfo.getIsSystemTable();
				})
				.map(fieldInfo -> {
					String prefix = "Datarouter node "
							+ clients.getClientTypeInstance(fieldInfo.getClientId()).getName()
							+ " "
							+ fieldInfo.getClientId().getName()
							+ " "
							+ fieldInfo.getNodeName();
					return MetricName.availableMetric(fieldInfo.getNodeName(), prefix);
				})
				.list();
	}

	private List<MetricName> getJobMetricNames(boolean showSystemInfo){
		return Scanner.of(injector.getInstances(triggerGroupClasses.get()))
				.include(triggerGroup -> {
					if(showSystemInfo){
						return triggerGroup.isSystemTriggerGoup;
					}
					return !triggerGroup.isSystemTriggerGoup;
				})
				.map(BaseTriggerGroup::getJobPackages)
				.concat(Scanner::of)
				.map(JobPackage::toString)
				.map(name -> {
					String prefix = "Datarouter job " + name;
					return MetricName.availableMetric(name, prefix);
				})
				.list();
	}

	private List<MetricName> getHandlerMetricNames(boolean showSystemInfo){
		return Scanner.of(routeSetRegistry.get())
				.map(BaseRouteSet::getDispatchRules)
				.concat(Scanner::of)
				.include(dispatchRule -> {
					if(showSystemInfo){
						return dispatchRule.isSystemDispatchRule();
					}
					return !dispatchRule.isSystemDispatchRule();
				})
				.map(DispatchRule::getHandlerClass)
				.map(Class::getSimpleName)
				.distinct()
				.map(name -> {
					String classPrefix = "Datarouter handler class " + name;
					var metricNameHandlerClass = MetricName.exactMetric(name + " class", classPrefix, MetricType.COUNT);

					String methodPrefix = "Datarouter handler method " + name;
					var metricNameMethods = MetricName.availableMetric(name + " endpoints", methodPrefix);

					return List.of(metricNameHandlerClass, metricNameMethods);
				})
				.concat(Scanner::of)
				.list();
	}

}
