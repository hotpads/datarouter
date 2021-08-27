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
package io.datarouter.joblet.handler;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.i;
import static j2html.TagCreator.td;
import static j2html.TagCreator.text;

import java.util.Collection;

import javax.inject.Inject;

import io.datarouter.instrumentation.metric.MetricLinkBuilder;
import io.datarouter.joblet.DatarouterJobletCounters;
import io.datarouter.joblet.JobletPageFactory;
import io.datarouter.joblet.dto.JobletSummary;
import io.datarouter.joblet.enums.JobletStatus;
import io.datarouter.joblet.nav.JobletExternalLinkBuilder;
import io.datarouter.joblet.nav.JobletLocalLinkBuilder;
import io.datarouter.joblet.storage.jobletrequest.DatarouterJobletRequestDao;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.handler.types.optional.OptionalString;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;

public class JobletHandler extends BaseHandler{

	private static final String TITLE = "Joblets";
	public static final String PARAM_whereStatus = "whereStatus";

	@Inject
	private DatarouterJobletRequestDao jobletRequestDao;
	@Inject
	private JobletPageFactory pageFactory;
	@Inject
	private JobletLocalLinkBuilder localLinkBuilder;
	@Inject
	private JobletExternalLinkBuilder externalLinkBuilder;
	@Inject
	private MetricLinkBuilder metricLinkBuilder;

	@Handler
	private Mav list(@Param(PARAM_whereStatus) OptionalString pStatus){
		Scanner<JobletRequest> requests = jobletRequestDao.scan();
		if(pStatus.isPresent()){
			JobletStatus status = JobletStatus.fromPersistentStringStatic(pStatus.get());
			requests = requests.include(request -> status == request.getStatus());
		}
		Collection<JobletSummary> summaries = JobletSummary.summarizeByTypeExecutionOrderStatus(requests);
		return pageFactory.startBuilder(request)
				.withTitle(TITLE)
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(makeContent(summaries))
				.buildMav();
	}

	private ContainerTag<?> makeContent(Collection<JobletSummary> rows){
		String contextPath = request.getContextPath();
		var title = h4(TITLE)
				.withClass("mt-2");
		var table = new J2HtmlTable<JobletSummary>()
				.withClasses("sortable table table-sm table-striped border")
				.withHtmlColumn("Type", row -> {
					String metricNamePrefix = DatarouterJobletCounters.makeQueueLengthJobletsCreatedPrefix(row
							.getType());
					String text = row.getType();
					return externalLinkBuilder.counters(metricNamePrefix)
							.map(href -> td(a(text).withHref(href)))
							.orElse(td(text));
				})
				.withColumn("Execution order", row -> row.getExecutionOrder())
				.withColumn("Status", row -> row.getStatus().getPersistentString())
				.withHtmlColumn("Num Joblets", row -> tdAlignRight(NumberFormatter.addCommas(row.getNumType())))
				.withHtmlColumn("Sum items", row -> tdAlignRight(NumberFormatter.addCommas(row.getSumItems())))
				.withHtmlColumn("Avg items", row -> tdAlignRight(NumberFormatter.format(row.getAvgItems(), 1)))
				.withHtmlColumn("Queues", row -> {
					String href = localLinkBuilder.queues(
							contextPath,
							row.getType(),
							row.getExecutionOrder(),
							row.getNumQueueIds());
					return tdAlignRight(a(row.getNumQueueIds() + "").withHref(href));
				})
				.withHtmlColumn("Failures", row -> {
					String href = localLinkBuilder.exceptions(contextPath, row.getType());
					return tdAlignRight(a(row.getNumFailures() + "").withHref(href));
				})
				.withHtmlColumn("First reserved", row -> {
					return tdAlignRight(row.getFirstReservedAgo())
							.attr("sorttable_customkey", row.getFirstReservedMsAgo());
				})
				.withHtmlColumn("First created", row -> {
					return tdAlignRight(row.getFirstCreatedAgo())
							.attr("sorttable_customkey", row.getFirstCreatedMsAgo());
				})
				.withHtmlColumn("", row -> {
					var chartIcon = i().withClass("fas fa-chart-line");
					var href = metricLinkBuilder.availableMetricsLink("Joblet .* " + row.getType() + "$");
					return td(a(chartIcon).withHref(href));
				})
				.withHtmlColumn("X", row -> {
					var trashIcon = i().withClass("fas fa-trash");
					String href = localLinkBuilder.delete(
							contextPath,
							row.getType(),
							row.getExecutionOrder(),
							row.getStatus());
					return td(a(trashIcon).withHref(href));
				})
				.build(rows);
		return div(title, table)
				.withClass("container-fluid");
	}

	private ContainerTag<?> tdAlignRight(String text){
		return tdAlignRight(text(text));
	}

	private ContainerTag<?> tdAlignRight(DomContent content){
		return td(content)
				.withStyle("text-align:right");
	}

}
