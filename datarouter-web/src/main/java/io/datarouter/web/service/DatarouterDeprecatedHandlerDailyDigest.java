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
package io.datarouter.web.service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import io.datarouter.auth.config.DatarouterAuthPaths;
import io.datarouter.instrumentation.relay.rml.Rml;
import io.datarouter.instrumentation.relay.rml.RmlBlock;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.tag.Tag;
import io.datarouter.web.config.RouteSetRegistry;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestRmlService;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.dispatcher.RouteSet;
import io.datarouter.web.handler.BaseHandler.Handler;
import io.datarouter.web.handler.HandlerTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterDeprecatedHandlerDailyDigest implements DailyDigest{

	private static final String HANDLER_CATEGORY = "handler";
	private static final String DEPRECATED_CATEGORY = "deprecated";

	@Inject
	private DatarouterAuthPaths paths;
	@Inject
	private DailyDigestRmlService digestService;
	@Inject
	private RouteSetRegistry routeSetRegistry;

	@Override
	public String getTitle(){
		return "Deprecated Handlers";
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.ACTIONABLE;
	}

	@Override
	public DailyDigestGrouping getGrouping(){
		return DailyDigestGrouping.LOW;
	}


	private record DeprecatedHandlerMethod(String methodName, String className, String deprecatedOn){}

	@Override
	public Optional<RmlBlock> getRelayContent(ZoneId zoneId){
		List<DeprecatedHandlerMethod> pastDeprecatedHandlerMethods = getPastDeprecatedHandlerMethods();
		if(pastDeprecatedHandlerMethods.isEmpty()){
			return Optional.empty();
		}
		return Optional.of(Rml.paragraph(
				digestService.makeHeading("Past Deprecated Handlers", paths.docs),
				Rml.text("Handlers marked as deprecated with a date in the past").italic(),
				Rml.table(
						Rml.tableRow(
								Rml.tableHeader(Rml.text("Class")),
								Rml.tableHeader(Rml.text("Method")),
								Rml.tableHeader(Rml.text("DeprecationDate"))))
						.with(pastDeprecatedHandlerMethods.stream()
								.map(method -> Rml.tableRow(
										Rml.tableCell(Rml.text(method.className)),
										Rml.tableCell(Rml.text(method.methodName)),
										Rml.tableCell(Rml.text(method.deprecatedOn)))))));
	}

	@Override
	public List<DailyDigestPlatformTask> getTasks(ZoneId zoneId){
		return Scanner.of(getPastDeprecatedHandlerMethods())
				.map(deprecated -> new DailyDigestPlatformTask(
						List.of(HANDLER_CATEGORY, DEPRECATED_CATEGORY, deprecated.className, deprecated.methodName),
						List.of(HANDLER_CATEGORY, DEPRECATED_CATEGORY),
						"Deprecation date exceeded for " + deprecated.methodName + " in " + deprecated.className,
						Rml.paragraph(
								Rml.text("Handler "), Rml.text(deprecated.methodName).code(), Rml.text(" in "),
								Rml.text(deprecated.className).code(), Rml.text(" has a deprecation date of "),
								Rml.timestamp(
										deprecated.deprecatedOn,
										HandlerTool.parseHandlerDeprecatedOnDate(deprecated.deprecatedOn)
												.toEpochMilli()))))
				.list();
	}

	private List<DeprecatedHandlerMethod> getPastDeprecatedHandlerMethods(){
		return Scanner.of(routeSetRegistry.get())
				.concatIter(RouteSet::getDispatchRulesNoRedirects)
				.include(rule -> rule.getTag() == Tag.APP)
				.map(DispatchRule::getHandlerClass)
				.distinct()
				.map(Class::getDeclaredMethods)
				.concat(Scanner::of)
				.include(method -> method.getAnnotation(Handler.class) != null)
				.exclude(method -> method.getAnnotation(Handler.class).deprecatedOn().isEmpty())
				.include(method -> HandlerTool.parseHandlerDeprecatedOnDate(
								method.getAnnotation(Handler.class).deprecatedOn())
						.isBefore(Instant.now()))
				.map(method -> new DeprecatedHandlerMethod(
						method.getName(),
						method.getDeclaringClass().getSimpleName(),
						method.getAnnotation(Handler.class).deprecatedOn()))
				.list();
	}

}
