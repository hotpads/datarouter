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
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;

import io.datarouter.auth.service.DatarouterUserService;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser;
import io.datarouter.instrumentation.metric.MetricLinkBuilder;
import io.datarouter.metric.service.HandlerUsageService;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.handlerusage.HandlerUsageBuilder.HandlerUsageDto;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlForm.HtmlFormMethod;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import j2html.tags.DomContent;
import j2html.tags.specialized.TableTag;
import j2html.tags.specialized.TdTag;
import jakarta.inject.Inject;

public class HandlerUsageHandler extends BaseHandler{
	private static final String HEADER = "Handler Usage";
	private static final String DEFAULT_DURATION = "1d";
	private static final String P_duration = "duration";
	private static final String P_groupBy = "groupBy";
	private static final List<String> GROUP_BY_OPTIONS = Scanner.of(GroupBy.values()).map(Enum::name).list();

	private enum GroupBy{
		CLASS,
		ROUTE_SET,
		USAGE
	}

	@Inject
	private MetricLinkPageFactory pageFactory;
	@Inject
	private DatarouterUserService userService;
	@Inject
	private MetricLinkBuilder linkBuilder;
	@Inject
	private HandlerUsageService handlerUsageService;

	@Handler
	private Mav view(@Param(P_duration) Optional<String> duration, @Param(P_groupBy) Optional<String> groupBy){
		DatarouterUser datarouterUser = userService
				.getAndValidateCurrentUser(getSessionInfo().getRequiredSession());
		String durationValidationError = null;
		DatarouterDuration datarouterDuration = new DatarouterDuration(DEFAULT_DURATION);
		if(duration.isPresent()){
			try{
				datarouterDuration = new DatarouterDuration(duration.get());
			}catch(Exception e){
				durationValidationError = "Duration must be in the following format: 1d1h1m1s1ms";
			}
		}

		List<HandlerUsageDto> handlerUsageMetrics = handlerUsageService.getHandlerUsageMetrics(datarouterDuration,
				datarouterUser.getUsername());

		DomContent content = makePageContent(
				handlerUsageMetrics,
				datarouterDuration,
				durationValidationError,
				groupBy.orElse(GroupBy.CLASS.name()));
		return pageFactory.simplePage(request, HEADER, content);
	}

	private DomContent makePageContent(
			List<HandlerUsageDto> usageDtos,
			DatarouterDuration duration,
			String durationValidationError,
			String groupBy){
		var form = new HtmlForm(HtmlFormMethod.GET);
		form.addTextField()
				.withLabel("Duration")
				.withName(P_duration)
				.withPlaceholder("Time Window (1d1h1m1s1ms)")
				.withValue(duration.toString())
				.withError(durationValidationError);
		form.addSelectField()
				.withLabel("Group By")
				.withName(P_groupBy)
				.withValues(GROUP_BY_OPTIONS)
				.withSelected(groupBy);
		form.addButton()
				.withLabel("Update")
				.withValue("view");

		int sumZeroCount = Scanner.of(usageDtos)
				.map(HandlerUsageDto::invocations)
				.include(invocations -> invocations == 0)
				.countInt();

		return div().with(
				h2(HEADER),
				p("Metric period is pinned to 1d to keep queries efficient."),
				Bootstrap4FormHtml.render(form).withClass("card card-body bg-light"),
				br(),
				p("Total unused handler methods: " + sumZeroCount).withStyle("font-weight:bold"))
				.with(makeTablesByGroup(groupBy, usageDtos))
				.with(br())
				.withClass("container-fluid mt-3");
	}

	private Function<HandlerUsageDto,String> getGroupByMappingFunction(String groupBy){
		return switch(GroupBy.valueOf(groupBy)){
			case CLASS -> HandlerUsageDto::classSimpleName;
			case ROUTE_SET -> dto -> handlerUsageService
					.getRouteSetNamesByClass()
					.get(dto.classSimpleName());
			case USAGE -> dto -> dto.invocations() == 0.0 ? "Unused Method" : "Used Method";
		};
	}

	private List<TableTag> makeTablesByGroup(String groupBy, List<HandlerUsageDto> usageDtos){
		Map<String,List<HandlerUsageDto>> methodInvocationsByGroup = Scanner.of(usageDtos)
				.groupBy(
						getGroupByMappingFunction(groupBy),
						Function.identity(),
						TreeMap::new);
		List<TableTag> handlerTables = new ArrayList<>();
		boolean includeClassName = !GroupBy.valueOf(groupBy).equals(GroupBy.CLASS);
		methodInvocationsByGroup.forEach((groupName, dtoList) ->
				handlerTables.add(makeHandlerGroupTable(groupName, dtoList, includeClassName)));
		return handlerTables;
	}

	private TableTag makeHandlerGroupTable(String groupName, List<HandlerUsageDto> dtoList, boolean includeClassName){
		J2HtmlTable<HandlerUsageDto> baseTable = new J2HtmlTable<>();
		if(includeClassName){
			baseTable.withHtmlColumn(th("Handler Class"), this::makeClassNameRow);
		}
		baseTable
				.withClasses("sortable container-fluid table table-bordered table-sm table-striped table-hover")
				.withHtmlColumn(th(groupName), this::makeHandlerNameRow)
				.withHtmlColumn(th("Invocations"), this::makeInvocationCountRow);

		return baseTable.build(dtoList);
	}

	private TdTag makeHandlerNameRow(HandlerUsageDto dto){
		return td(a(dto.methodName())
				.withHref(linkBuilder.exactMetricLink(dto.alias())));
	}

	private TdTag makeInvocationCountRow(HandlerUsageDto dto){
		return td(Integer.toString(dto.invocations().intValue()))
				.withCondStyle(dto.invocations() == 0, "color:#C41E3A;font-weight:bold;");
	}

	private TdTag makeClassNameRow(HandlerUsageDto dto){
		return td(a(dto.classSimpleName()));
	}

}
