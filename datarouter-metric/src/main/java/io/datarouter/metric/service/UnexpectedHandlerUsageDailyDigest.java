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

import static j2html.TagCreator.div;

import java.lang.reflect.Method;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.instrumentation.relay.rml.Rml;
import io.datarouter.instrumentation.relay.rml.RmlBlock;
import io.datarouter.metric.config.DatarouterMetricPaths;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.tag.Tag;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.web.config.RouteSetRegistry;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestService;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.dispatcher.RouteSet;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.BaseHandler.Handler;
import io.datarouter.web.handler.BaseHandler.Handler.HandlerUsageType;
import io.datarouter.web.handlerusage.HandlerUsageBuilder.HandlerUsageDto;
import j2html.TagCreator;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.SmallTag;
import j2html.tags.specialized.TableTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class UnexpectedHandlerUsageDailyDigest implements DailyDigest{
	private static final int ROW_DISPLAY_LIMIT = 5;
	private static final long DAYS_TO_QUERY = 30L;
	private static final String PATH_SUPPLEMENT = "?duration=30d&groupBy=USAGE";
	private static final SmallTag CAPTION_UNUSED = TagCreator.small("Handlers with no invocations in the past 30 days "
			+ "should be investigated and removed from code if no longer needed. If still in use but just infrequently,"
			+ " set \"usageType\" to INFREQUENTLY_USED on the @Handler annotation to omit them from this report.");
	private static final SmallTag CAPTION_TEMPORARILY_UNUSED = TagCreator.small("The following handlers are marked as "
			+ "temporarily unused but have been used in the past 30 days. If they are in use but infrequently, set "
			+ "\"usageType\" to TEMPORARILY_UNUSED on the @Handler annotation to omit them from this report, otherwise "
			+ "if fully in-use, remove the usage type field completely.");

	@Inject
	private DailyDigestService dailyDigestService;
	@Inject
	private HandlerUsageService handlerUsageService;
	@Inject
	private DatarouterMetricPaths paths;
	@Inject
	private RouteSetRegistry routeSetRegistry;

	@Override
	public String getTitle(){
		return "Unexpected Handler Usage";
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.ACTIONABLE;
	}

	@Override
	public DailyDigestGrouping getGrouping(){
		return DailyDigestGrouping.MEDIUM;
	}

	@Override
	public Optional<DivTag> getEmailContent(ZoneId zoneId){
		List<HandlerUsageDto> handlerUsageMetrics = handlerUsageService.getHandlerUsageMetrics(
				new DatarouterDuration(DAYS_TO_QUERY, TimeUnit.DAYS),
				getClass().getSimpleName());

		List<? extends Class<? extends BaseHandler>> handlerClasses = Scanner.of(routeSetRegistry.get())
				.concatIter(RouteSet::getDispatchRulesNoRedirects)
				.include(rule -> rule.getTag() == Tag.APP)
				.map(DispatchRule::getHandlerClass)
				.list();

		List<UnusedHandlerMethod> unusedHandlerMethods = Scanner.of(handlerUsageMetrics)
				.include(metricDto -> metricDto.invocations() == 0)
				.exclude(metricDto -> isMarkedAsInfrequentlyUsed(handlerClasses, metricDto))
				.exclude(metricDto -> isMarkedAsTemporarilyUnused(handlerClasses, metricDto))
				.map(metricDto -> new UnusedHandlerMethod(
						metricDto.methodName(),
						metricDto.classSimpleName()))
				.list();

		List<UnusedHandlerMethod> usedButMarkedAsTemporarilyUnused = Scanner.of(handlerUsageMetrics)
				.include(metricDto -> metricDto.invocations() > 0)
				.include(metricDto -> isMarkedAsTemporarilyUnused(handlerClasses, metricDto))
				.map(metricDto -> new UnusedHandlerMethod(
						metricDto.methodName(),
						metricDto.classSimpleName()))
				.list();

		if(unusedHandlerMethods.isEmpty() && usedButMarkedAsTemporarilyUnused.isEmpty()){
			return Optional.empty();
		}

		return Optional.of(div(
				buildUnusedHandlerMethodsSection(unusedHandlerMethods),
				buildUnexpectHandlerUsageSection(usedButMarkedAsTemporarilyUnused)));
	}

	private DivTag buildUnusedHandlerMethodsSection(List<UnusedHandlerMethod> unusedHandlerMethods){
		if(unusedHandlerMethods.isEmpty()){
			return new DivTag();
		}
		var unusedHandlerMethodsHeader = dailyDigestService.makeHeader(
				generateTruncatedHeaderString(unusedHandlerMethods, "Handlers with no invocations in the past 30 days: "
						+ unusedHandlerMethods.size()),
				paths.datarouter.metric.handlerUsage.view,
				PATH_SUPPLEMENT);
		TableTag emailTable = buildEmailTable(unusedHandlerMethods);
		return div(unusedHandlerMethodsHeader, CAPTION_UNUSED, emailTable);
	}

	private DivTag buildUnexpectHandlerUsageSection(List<UnusedHandlerMethod> usedButMarkedAsTemporarilyUnused){
		if(usedButMarkedAsTemporarilyUnused.isEmpty()){
			return new DivTag();
		}
		var unexpectHandlerUsageHeader = dailyDigestService.makeHeader(
				generateTruncatedHeaderString(usedButMarkedAsTemporarilyUnused, "Handlers marked as temporarily unused"
						+ " but with invocations in the past 30 days: " + usedButMarkedAsTemporarilyUnused.size()),
				paths.datarouter.metric.handlerUsage.view,
				PATH_SUPPLEMENT);
		TableTag emailTable = buildEmailTable(usedButMarkedAsTemporarilyUnused);
		return div(unexpectHandlerUsageHeader, CAPTION_TEMPORARILY_UNUSED, emailTable);
	}

	private static String generateTruncatedHeaderString(List<UnusedHandlerMethod> methods, String header){
		header += methods.size() > ROW_DISPLAY_LIMIT
				? " (first " + ROW_DISPLAY_LIMIT + " shown)"
				: "";
		return header;
	}

	@Override
	public Optional<RmlBlock> getRelayContent(ZoneId zoneId){
		List<HandlerUsageDto> handlerUsageMetrics = handlerUsageService.getHandlerUsageMetrics(
				new DatarouterDuration(DAYS_TO_QUERY, TimeUnit.DAYS),
				getClass().getSimpleName());

		List<? extends Class<? extends BaseHandler>> handlerClasses = Scanner.of(routeSetRegistry.get())
				.concatIter(RouteSet::getDispatchRulesNoRedirects)
				.include(rule -> rule.getTag() == Tag.APP)
				.map(DispatchRule::getHandlerClass)
				.list();

		List<UnusedHandlerMethod> unusedHandlerMethods = Scanner.of(handlerUsageMetrics)
				.include(metricDto -> metricDto.invocations() == 0)
				.exclude(metricDto -> isMarkedAsInfrequentlyUsed(handlerClasses, metricDto))
				.map(metricDto -> new UnusedHandlerMethod(
						metricDto.methodName(),
						metricDto.classSimpleName()))
				.list();

		List<UnusedHandlerMethod> usedButMarkedAsTemporarilyUnused = Scanner.of(handlerUsageMetrics)
				.include(metricDto -> metricDto.invocations() > 0)
				.include(metricDto -> isMarkedAsTemporarilyUnused(handlerClasses, metricDto))
				.map(metricDto -> new UnusedHandlerMethod(
						metricDto.methodName(),
						metricDto.classSimpleName()))
				.list();

		if(unusedHandlerMethods.isEmpty() && usedButMarkedAsTemporarilyUnused.isEmpty()){
			return Optional.empty();
		}

		return Optional.of(Rml.paragraph(
				dailyDigestService.makeHeading(
						"Handlers with no invocations in the past 30 days: " + unusedHandlerMethods.size(),
						paths.datarouter.metric.handlerUsage.view),
				Rml.text("Handlers with no invocations in the past 30 days should be investigated and removed from "
						+ "code if no longer needed. If still in use but just infrequently, set \"usageType\" to  "
						+ "INFREQUENTLY_USED on the @Handler annotation to omit them from this report.").italic(),
				Rml.table(
								Rml.tableRow(
										Rml.tableHeader(Rml.text("Class")),
										Rml.tableHeader(Rml.text("Method"))))
						.with(unusedHandlerMethods.stream()
								.map(method -> Rml.tableRow(
										Rml.tableCell(Rml.text(method.className)),
										Rml.tableCell(Rml.text(method.methodName))))),
				dailyDigestService.makeHeading(
						"Handlers marked as temporarily unused but with invocations in the past 30 days: "
								+ usedButMarkedAsTemporarilyUnused.size(),
						paths.datarouter.metric.handlerUsage.view),
				Rml.text("The following handlers are marked as temporarily unused but have been used in the past 30 "
						+ "days. If they are in use but infrequently, set \"usageType\" to INFREQUENTLY_USED on the "
						+ "@Handler annotation to omit them from this report, otherwise if fully in-use, remove the "
						+ "usage type field completely.").italic(),
				Rml.table(
								Rml.tableRow(
										Rml.tableHeader(Rml.text("Class")),
										Rml.tableHeader(Rml.text("Method"))))
						.with(unusedHandlerMethods.stream()
								.map(method -> Rml.tableRow(
										Rml.tableCell(Rml.text(method.className)),
										Rml.tableCell(Rml.text(method.methodName)))))));
	}

	private boolean isMarkedAsInfrequentlyUsed(
			List<? extends Class<? extends BaseHandler>> handlerClasses,
			HandlerUsageDto metricDto){
		return Scanner.of(handlerClasses)
				.include(handlerClass -> handlerClass.getSimpleName().equals(metricDto.classSimpleName()))
				.concatIter(handlerClass ->
						ReflectionTool.getDeclaredMethodsWithAnnotation(handlerClass, Handler.class))
				.include(method -> method.getAnnotation(Handler.class).usageType()
						== HandlerUsageType.INFREQUENTLY_USED)
				.map(Method::getName)
				.list()
				.contains(metricDto.methodName());
	}

	private boolean isMarkedAsTemporarilyUnused(
			List<? extends Class<? extends BaseHandler>> handlerClasses,
			HandlerUsageDto metricDto){
		return Scanner.of(handlerClasses)
				.include(handlerClass -> handlerClass.getSimpleName().equals(metricDto.classSimpleName()))
				.concatIter(handlerClass ->
						ReflectionTool.getDeclaredMethodsWithAnnotation(handlerClass, Handler.class))
				.include(method -> method.getAnnotation(Handler.class).usageType()
						== HandlerUsageType.TEMPORARILY_UNUSED)
				.map(Method::getName)
				.list()
				.contains(metricDto.methodName());
	}

	private record UnusedHandlerMethod(String methodName, String className){}

	private static TableTag buildEmailTable(
			List<UnusedHandlerMethod> rows){
		rows = rows.size() > ROW_DISPLAY_LIMIT ? rows.subList(0, ROW_DISPLAY_LIMIT) : rows;
		return new J2HtmlEmailTable<UnusedHandlerMethod>()
				.withColumn("Class", row -> row.className)
				.withColumn("Method", row -> row.methodName)
				.build(rows);
	}

}
