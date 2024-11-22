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

import static j2html.TagCreator.div;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.datarouter.auth.config.DatarouterAuthPaths;
import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.instrumentation.relay.rml.Rml;
import io.datarouter.instrumentation.relay.rml.RmlBlock;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.web.config.RouteSetRegistry;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestService;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.dispatcher.RouteSet;
import io.datarouter.web.handler.BaseHandler.Handler;
import io.datarouter.web.handler.HandlerTool;
import j2html.TagCreator;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.SmallTag;
import j2html.tags.specialized.TableTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterDeprecatedHandlerDailyDigest implements DailyDigest{

	private static final SmallTag CAPTION = TagCreator.small("Handlers existing past their deprecation date should be"
			+ " removed from code, or their date updated.");

	@Inject
	private DatarouterAuthPaths paths;
	@Inject
	private DailyDigestService digestService;
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

	@Override
	public Optional<DivTag> getEmailContent(ZoneId zoneId){
		List<DeprecatedHandlerMethod> pastDeprecatedHandlerMethods = getPastDeprecatedHandlerMethods();
		if(pastDeprecatedHandlerMethods.isEmpty()){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Handlers marked as deprecated with a date in the past", paths.docs);
		var table = buildEmailTable(pastDeprecatedHandlerMethods);
		return Optional.of(div(header, CAPTION, table));
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
										Rml.tableCell(Rml.timestamp(
												method.deprecatedOn,
												HandlerTool.parseHandlerDeprecatedOnDate(method.deprecatedOn)
														.toEpochMilli())))))));
	}

	private List<DeprecatedHandlerMethod> getPastDeprecatedHandlerMethods(){
		List<Method> handlerMethods = Scanner.of(routeSetRegistry.get())
				.concatIter(RouteSet::getDispatchRulesNoRedirects)
				.map(DispatchRule::getHandlerClass)
				.concatIter(handlerClass ->
						ReflectionTool.getDeclaredMethodsWithAnnotation(handlerClass, Handler.class))
				.list();
		List<DeprecatedHandlerMethod> pastDeprecatedHandlerMethods = new ArrayList<>();
		Scanner.of(handlerMethods)
				.exclude(method -> method.getAnnotation(Handler.class).deprecatedOn().isEmpty())
				.include(method -> HandlerTool.parseHandlerDeprecatedOnDate(
								method.getAnnotation(Handler.class).deprecatedOn())
						.isBefore(Instant.now().minus(21, ChronoUnit.DAYS)))
				.forEach(method -> pastDeprecatedHandlerMethods.add(new DeprecatedHandlerMethod(
						method.getName(),
						method.getDeclaringClass().getName(),
						method.getAnnotation(Handler.class).deprecatedOn())));
		return pastDeprecatedHandlerMethods;
	}

	private static TableTag buildEmailTable(List<DatarouterDeprecatedHandlerDailyDigest.DeprecatedHandlerMethod> rows){
		return new J2HtmlEmailTable<DeprecatedHandlerMethod>()
				.withColumn("Class", row -> row.className)
				.withColumn("Method", row -> row.methodName)
				.withColumn("Deprecation Date", row -> row.deprecatedOn)
				.build(rows);
	}

}
