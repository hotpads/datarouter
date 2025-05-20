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

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.datarouter.instrumentation.relay.rml.Rml;
import io.datarouter.instrumentation.relay.rml.RmlBlock;
import io.datarouter.metric.config.DatarouterMetricPaths;
import io.datarouter.metric.dashboard.web.HandlerUsageHandler.GroupBy;
import io.datarouter.metric.link.HandlerUsageLink;
import io.datarouter.metric.service.HandlerUsageService.ActionableHandlers;
import io.datarouter.metric.service.HandlerUsageService.HandlerMethodNameAndClassName;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestRmlService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class UnexpectedHandlerUsageDailyDigest implements DailyDigest{

	private static final long DAYS_TO_QUERY = 30L;
	private static final String
			HANDLER_CATEGORY = "handler",
			UNUSED_CATEGORY = "unused",
			ACTIVITY_CATEGORY = "activity",
			NON_PROD_USAGE = "nonProdUsage",
			UNUSED_HEADING_PREFIX = "Handlers with no invocations in the past 30 days: ",
			TEMPORARILY_UNUSED_ACTIVITY_HEADING_PREFIX = String.format("Handlers marked as temporarily unused but with "
							+ "invocations in the past %d days: ", DAYS_TO_QUERY),
			NON_PROD_ONLY_ACTIVITY_HEADING_PREFIX = String.format("Handlers marked as non-prod only but with prod "
							+ "invocations in the past %d days: ", DAYS_TO_QUERY),
			CAPTION_UNUSED = String.format("""
					Handlers with no invocations in the past %d days should be investigated and removed from code if no
					longer needed. If still in use but just infrequently, set "usageType" to INFREQUENTLY_USED on the
					@Handler annotation to omit them from this report. If only used in non-prod environments, mark as
					NON_PROD_ONLY. If temporarily unused, perhaps due to being before/mid feature launch, mark
					TEMPORARILY_UNUSED. Example annotation: @Handler(usageType = HandlerUsageType.NON_PROD_ONLY)""",
					DAYS_TO_QUERY),
			CAPTION_ACTIVITY_TEMPORARILY_UNUSED = String.format("""
					The following handlers are marked as temporarily unused but have been used in the past %d days.
					If they are in use but infrequently, set "usageType" to INFREQUENTLY_USED on the @Handler annotation
					to omit them from this report. If only used in non-prod environments, mark as NON_PROD_ONLY.
					Otherwise if fully in-use, remove the usage type field completely.""",
					DAYS_TO_QUERY),
			CAPTION_ACTIVITY_NON_PROD_ONLY = String.format("""
					The following handlers are marked for non-prod use only but have been invoked in production in the
					past %d days. Please review these handlers and update with the appropriate usage type.""",
					DAYS_TO_QUERY);

	@Inject
	private DailyDigestRmlService dailyDigestService;
	@Inject
	private HandlerUsageService handlerUsageService;
	@Inject
	private DatarouterMetricPaths paths;

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
	public Optional<RmlBlock> getRelayContent(ZoneId zoneId){
		ActionableHandlers actionableHandlers = handlerUsageService.getActionableHandlers(DAYS_TO_QUERY);

		List<HandlerMethodNameAndClassName> unusedHandlerMethods = actionableHandlers.unusedHandlerMethods();
		List<HandlerMethodNameAndClassName> usedButMarkedAsTemporarilyUnused = actionableHandlers
				.usedButMarkedAsTemporarilyUnused();
		List<HandlerMethodNameAndClassName> usedButMarkedAsNonProdOnly = actionableHandlers
				.usedButMarkedAsNonProdOnly();

		if(unusedHandlerMethods.isEmpty()
				&& usedButMarkedAsTemporarilyUnused.isEmpty()
				&& usedButMarkedAsNonProdOnly.isEmpty()){
			return Optional.empty();
		}

		return Optional.of(Rml.container()
				.condWith(
						!unusedHandlerMethods.isEmpty(),
						buildHandlerMethodsSectionRelay(unusedHandlerMethods, UNUSED_HEADING_PREFIX, CAPTION_UNUSED))
				.condWith(
						!usedButMarkedAsTemporarilyUnused.isEmpty(),
						buildHandlerMethodsSectionRelay(usedButMarkedAsTemporarilyUnused,
								TEMPORARILY_UNUSED_ACTIVITY_HEADING_PREFIX, CAPTION_ACTIVITY_TEMPORARILY_UNUSED))
				.condWith(
						!usedButMarkedAsNonProdOnly.isEmpty(),
						buildHandlerMethodsSectionRelay(usedButMarkedAsNonProdOnly,
								NON_PROD_ONLY_ACTIVITY_HEADING_PREFIX, CAPTION_ACTIVITY_NON_PROD_ONLY)));
	}

	@Override
	public List<DailyDigestPlatformTask> getTasks(ZoneId zoneId){
		ActionableHandlers actionableHandlers = handlerUsageService.getActionableHandlers(DAYS_TO_QUERY);

		return Scanner.concat(
				Scanner.of(actionableHandlers.unusedHandlerMethods())
						.map(unused -> new TaskDetails(
								List.of(UNUSED_CATEGORY),
								"Unused handler method",
								CAPTION_ACTIVITY_NON_PROD_ONLY,
								unused)),
				Scanner.of(actionableHandlers.usedButMarkedAsTemporarilyUnused())
						.map(unused -> new TaskDetails(
								List.of(UNUSED_CATEGORY, ACTIVITY_CATEGORY),
								"Activity for temporarily unused handler method",
								CAPTION_ACTIVITY_TEMPORARILY_UNUSED,
								unused)),
				Scanner.of(actionableHandlers.usedButMarkedAsNonProdOnly())
						.map(handler -> new TaskDetails(
								List.of(NON_PROD_USAGE),
								"Non-prod handler used in prod:",
								CAPTION_ACTIVITY_NON_PROD_ONLY,
								handler)))
				.map(details -> new DailyDigestPlatformTask(
						Scanner.of(HANDLER_CATEGORY)
								.append(details.categories())
								.append(details.method().className())
								.append(details.method().methodName())
								.list(),
						Scanner.of(HANDLER_CATEGORY)
								.append(details.categories())
								.list(),
						details.title() + " " + details.method().methodName() + " in " + details.method().className(),
						Rml.doc(
								Rml.heading(3, Rml.text(details.method().className() + "::"
										+ details.method().methodName())),
								Rml.paragraph(Rml.text(details.caption())),
								Rml.paragraph(dailyDigestService.makeLink("Handler usage details",
										paths.datarouter.metric.handlerUsage.view)))))
				.list();
	}

	private RmlBlock buildHandlerMethodsSectionRelay(List<HandlerMethodNameAndClassName> methods, String headingPrefix,
			String caption){
		return Rml.container(
				dailyDigestService.makeHeading(headingPrefix + methods.size(),
						new HandlerUsageLink()
								.withDuration(new DatarouterDuration(DAYS_TO_QUERY, TimeUnit.DAYS))
								.withGroupBy(GroupBy.USAGE_COUNT)
								.withExcludeIrregularUsage(true)),
				Rml.text(caption).italic(),
				Rml.table(
						Rml.tableRow(
								Rml.tableHeader(Rml.text("Class")),
								Rml.tableHeader(Rml.text("Method"))))
						.with(methods.stream()
								.map(method -> Rml.tableRow(
										Rml.tableCell(Rml.text(method.className())),
										Rml.tableCell(Rml.text(method.methodName()))))));
	}

	private record TaskDetails(
			List<String> categories,
			String title,
			String caption,
			HandlerMethodNameAndClassName method){
	}

}
