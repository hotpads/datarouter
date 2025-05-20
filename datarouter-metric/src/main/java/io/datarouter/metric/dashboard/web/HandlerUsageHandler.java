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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import io.datarouter.auth.service.DatarouterUserService;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser;
import io.datarouter.instrumentation.metric.MetricLinkBuilder;
import io.datarouter.metric.config.HandlerUsageExecutors.HandlerUsageExecutor;
import io.datarouter.metric.link.HandlerUsageLink;
import io.datarouter.metric.service.HandlerUsageService;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.types.MilliTime;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.BaseHandler.Handler.HandlerUsageType;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handlerusage.HandlerUsageBuilder.HandlerUsageQueryItemResponseDto;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlForm.HtmlFormMethod;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.tags.DomContent;
import j2html.tags.specialized.TableTag;
import j2html.tags.specialized.TdTag;
import jakarta.inject.Inject;

public class HandlerUsageHandler extends BaseHandler{
	private static final String HEADER = "Handler Usage";
	private static final String DEFAULT_DURATION = "1d";
	private static final List<String> GROUP_BY_OPTIONS = Scanner.of(GroupBy.values()).map(Enum::name).list();

	public enum GroupBy{
		CLASS,
		ROUTE_SET,
		USAGE_COUNT,
		USAGE_TYPE
	}

	@Inject
	private MetricLinkPageFactory pageFactory;
	@Inject
	private DatarouterUserService userService;
	@Inject
	private MetricLinkBuilder linkBuilder;
	@Inject
	private HandlerUsageService handlerUsageService;
	@Inject
	private HandlerUsageExecutor executor;

	@Handler
	private Mav view(HandlerUsageLink link){
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

		List<HandlerUsageQueryItemResponseDto> handlerUsageMetrics = handlerUsageService
				.getAllHandlerUsageMetrics(datarouterDuration, datarouterUser.getUsername(), new Threads(executor, 4));

		DomContent content = makePageContent(
				handlerUsageMetrics,
				datarouterDuration,
				durationValidationError,
				link.excludeIrregularUsage.orElse(false),
				link.groupBy.orElse(GroupBy.CLASS));
		return pageFactory.startBuilder(request)
				.withTitle(HEADER)
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(content)
				.buildMav();
	}

	private DomContent makePageContent(
			List<HandlerUsageQueryItemResponseDto> usageDtos,
			DatarouterDuration duration,
			String durationValidationError,
			Boolean excludeIrregularUsage,
			GroupBy groupBy){
		var form = new HtmlForm(HtmlFormMethod.GET);
		form.addTextField()
				.withLabel("Duration")
				.withName(HandlerUsageLink.P_duration)
				.withPlaceholder("Time Window (1d1h1m1s1ms)")
				.withValue(duration.toString())
				.withError(durationValidationError);
		form.addSelectField()
				.withLabel("Group By")
				.withName(HandlerUsageLink.P_groupBy)
				.withValues(GROUP_BY_OPTIONS)
				.withSelected(groupBy.name());
		form.addCheckboxField()
				.withLabel("Exclude handlers tagged for irregular usage")
				.withName(HandlerUsageLink.P_excludeIrregularUsage)
				.withChecked(excludeIrregularUsage);
		form.addButton()
				.withLabel("Update")
				.withValue("view");

		if(excludeIrregularUsage){
			usageDtos = Scanner.of(usageDtos)
					.include(dto -> dto.usageType() == HandlerUsageType.IN_USE)
					.list();
		}

		int sumZeroCount = Scanner.of(usageDtos)
				.map(HandlerUsageQueryItemResponseDto::invocations)
				.include(invocations -> invocations == 0)
				.countInt();

		return div().with(
				h2(HEADER),
				p("Metric period is pinned to 1d to keep queries efficient."),
				Bootstrap4FormHtml.render(form).withClass("card card-body bg-light"),
				br(),
				p("Total unused handler methods: " + sumZeroCount).withStyle("font-weight:bold"))
				.with(makeTablesByGroup(groupBy, usageDtos, duration))
				.with(br())
				.withClass("container-fluid mt-3");
	}

	private Function<HandlerUsageQueryItemResponseDto,String> getGroupByMappingFunction(GroupBy groupBy){
		return switch(groupBy){
			case CLASS -> HandlerUsageQueryItemResponseDto::classSimpleName;
			case ROUTE_SET -> dto -> handlerUsageService
					.getRouteSetNamesByClass()
					.get(dto.classSimpleName());
			case USAGE_COUNT -> dto -> dto.invocations() == 0.0 ? "Unused Method" : "Used Method";
			case USAGE_TYPE -> dto -> dto.usageType().name();
		};
	}

	private List<TableTag> makeTablesByGroup(GroupBy groupBy, List<HandlerUsageQueryItemResponseDto> usageDtos,
			DatarouterDuration duration){
		Map<String,List<HandlerUsageQueryItemResponseDto>> methodInvocationsByGroup = Scanner.of(usageDtos)
				.groupBy(
						getGroupByMappingFunction(groupBy),
						Function.identity(),
						TreeMap::new);
		List<TableTag> handlerTables = new ArrayList<>();
		boolean includeClassName = groupBy != GroupBy.CLASS;
		methodInvocationsByGroup.forEach((groupName, dtoList) ->
				handlerTables.add(makeHandlerGroupTable(groupName, dtoList, includeClassName, duration)));
		return handlerTables;
	}

	private TableTag makeHandlerGroupTable(
			String groupName,
			List<HandlerUsageQueryItemResponseDto> dtoList,
			boolean includeClassName,
			DatarouterDuration duration){
		J2HtmlTable<HandlerUsageQueryItemResponseDto> baseTable = new J2HtmlTable<>();
		if(includeClassName){
			baseTable.withHtmlColumn(th("Handler Class"), this::makeClassNameRow);
		}
		baseTable
				.withClasses("sortable table table-bordered table-sm table-striped")
				.withStyles("table-layout:fixed", "width:100%")
				.withHtmlColumn(th(groupName), dto -> makeHandlerNameRow(dto, duration))
				.withHtmlColumn(th("Usage Type"), this::makeHandlerUsageTypeRow)
				.withHtmlColumn(th("Invocations"), this::makeInvocationCountRow);

		return baseTable.build(dtoList);
	}

	private TdTag makeHandlerNameRow(HandlerUsageQueryItemResponseDto dto, DatarouterDuration duration){
		return td(a(dto.methodName())
				.withHref(linkBuilder.exactMetricLink(dto.alias(),
						MilliTime.now().minus(duration.toJavaDuration()).toEpochMilli(),
						MilliTime.now().toEpochMilli())));
	}

	private TdTag makeHandlerUsageTypeRow(HandlerUsageQueryItemResponseDto dto){
		return td(dto.usageType().name());
	}

	private TdTag makeInvocationCountRow(HandlerUsageQueryItemResponseDto dto){
		return td(Integer.toString(dto.invocations().intValue()))
				.withCondStyle(dto.invocations() == 0, "color:#C41E3A;font-weight:bold;");
	}

	private TdTag makeClassNameRow(HandlerUsageQueryItemResponseDto dto){
		return td(a(dto.classSimpleName()));
	}

}
