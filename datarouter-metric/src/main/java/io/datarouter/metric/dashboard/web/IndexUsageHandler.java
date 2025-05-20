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
package io.datarouter.metric.dashboard.web;

import static j2html.TagCreator.a;
import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.p;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;

import java.util.Comparator;
import java.util.List;

import io.datarouter.auth.service.DatarouterUserService;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser;
import io.datarouter.instrumentation.metric.MetricLinkBuilder;
import io.datarouter.metric.config.IndexUsageExecutors.IndexUsageExecutor;
import io.datarouter.metric.link.IndexUsageLink;
import io.datarouter.metric.service.IndexUsageService;
import io.datarouter.scanner.Threads;
import io.datarouter.types.MilliTime;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlForm.HtmlFormMethod;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.indexusage.IndexUsageBuilder.IndexUsageQueryItemResponseDto;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.tags.DomContent;
import j2html.tags.specialized.TableTag;
import j2html.tags.specialized.TdTag;
import jakarta.inject.Inject;

public class IndexUsageHandler extends BaseHandler{

	private static final String HEADER = "Index Usage";
	private static final String DEFAULT_DURATION = "1d";

	@Inject
	private MetricLinkPageFactory pageFactory;
	@Inject
	private DatarouterUserService userService;
	@Inject
	private MetricLinkBuilder linkBuilder;
	@Inject
	private IndexUsageService indexUsageService;
	@Inject
	private IndexUsageExecutor executor;

	@Handler
	public Mav view(IndexUsageLink link){
		DatarouterUser datarouterUser = userService
				.getAndValidateCurrentUser(getSessionInfo().getRequiredSession());
		String durationValidationError = null;
		DatarouterDuration datarouterDuration = new DatarouterDuration(DEFAULT_DURATION);
		if(link.duration.isPresent()){
			try{
				datarouterDuration = new DatarouterDuration(link.duration.get());
			}catch(Exception e){
				durationValidationError = "Duration must be in the following format: 1d1h1m1s1ms";
			}
		}

		List<IndexUsageQueryItemResponseDto> indexUsageMetrics = indexUsageService
				.getAllIndexUsageMetrics(datarouterDuration, datarouterUser.getUsername(), new Threads(executor, 2))
				.stream()
				.sorted(Comparator.comparing(IndexUsageQueryItemResponseDto::indexName))
				.toList();

		DomContent content = makePageContent(indexUsageMetrics, datarouterDuration, durationValidationError);
		return pageFactory.startBuilder(request)
				.withTitle(HEADER)
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(content)
				.buildMav();
	}

	private DomContent makePageContent(
			List<IndexUsageQueryItemResponseDto> usageDtos,
			DatarouterDuration duration,
			String durationValidationError){
		var form = new HtmlForm(HtmlFormMethod.GET);
		form.addTextField()
				.withLabel("Duration")
				.withName(IndexUsageLink.P_duration)
				.withPlaceholder("Time Window (1d1h1m1s1ms)")
				.withValue(duration.toString())
				.withError(durationValidationError);
		form.addButton()
				.withLabel("Update")
				.withValue("view");

		return div().with(
						h2(HEADER),
						p("Metric period is pinned to 1d to keep queries efficient."),
						Bootstrap4FormHtml.render(form).withClass("card card-body bg-light"),
						br(),
						makeIndexUsageTable(usageDtos, duration))
				.with(br())
				.withClass("container-fluid mt-3");
	}

	private TableTag makeIndexUsageTable(List<IndexUsageQueryItemResponseDto> usageDtos, DatarouterDuration duration){
		J2HtmlTable<IndexUsageQueryItemResponseDto> table = new J2HtmlTable<>();
		table.withClasses("sortable table table-bordered table-sm table-striped")
				.withStyles("table-layout:fixed", "width:100%")
				.withHtmlColumn(th("Index Name"), dto -> makeIndexNameRow(dto, duration))
				.withHtmlColumn(th("Usage Type"), this::makeUsageTypeRow)
				.withHtmlColumn(th("Metric Count"), this::makeInvocationCountRow)
				.withCaption("Total " + usageDtos.size());
		return table.build(usageDtos);
	}

	private TdTag makeIndexNameRow(IndexUsageQueryItemResponseDto dto, DatarouterDuration duration){
		return td(a(dto.indexName())
				.withHref(linkBuilder.exactMetricLink(indexUsageService.buildIndexMetricName(dto.indexName()),
						MilliTime.now().minus(duration.toJavaDuration()).toEpochMilli(),
						MilliTime.now().toEpochMilli())));
	}

	private TdTag makeUsageTypeRow(IndexUsageQueryItemResponseDto dto){
		return td(dto.usageType().name());
	}

	private TdTag makeInvocationCountRow(IndexUsageQueryItemResponseDto dto){
		return td(Long.toString(dto.count().longValue()))
				.withCondStyle(dto.count() == 0, "color:#C41E3A;font-weight:bold;");
	}

}
