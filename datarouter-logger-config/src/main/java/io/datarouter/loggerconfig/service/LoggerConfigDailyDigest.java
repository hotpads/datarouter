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
package io.datarouter.loggerconfig.service;

import static j2html.TagCreator.div;
import static j2html.TagCreator.small;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.instrumentation.relay.rml.Rml;
import io.datarouter.instrumentation.relay.rml.RmlBlock;
import io.datarouter.loggerconfig.config.DatarouterLoggingConfigPaths;
import io.datarouter.loggerconfig.storage.loggerconfig.DatarouterLoggerConfigDao;
import io.datarouter.loggerconfig.storage.loggerconfig.LoggerConfig;
import io.datarouter.types.MilliTime;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestService;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class LoggerConfigDailyDigest implements DailyDigest{

	@Inject
	private DatarouterLoggerConfigDao dao;
	@Inject
	private DailyDigestService digestService;
	@Inject
	private DatarouterLoggingConfigPaths paths;

	@Override
	public String getTitle(){
		return "Logger Configs";
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.SUMMARY;
	}

	@Override
	public DailyDigestGrouping getGrouping(){
		return DailyDigestGrouping.LOW;
	}

	@Override
	public Optional<DivTag> getEmailContent(ZoneId zoneId){
		List<LoggerConfig> loggers = getTodaysLoggers(zoneId);
		if(loggers.isEmpty()){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Logger Configs", paths.datarouter.logging);
		var description = small("Updated Today");
		var table = new J2HtmlEmailTable<LoggerConfig>()
				.withColumn("Name", row -> row.getKey().getName())
				.withColumn("Level", row -> row.getLevel().getPersistentString())
				.withColumn("User", LoggerConfig::getEmail)
				.withColumn("Updated", row -> row.getLastUpdated().format(zoneId))
				.build(loggers);
		return Optional.of(div(header, description, table));
	}

	@Override
	public Optional<RmlBlock> getRelayContent(ZoneId zoneId){
		List<LoggerConfig> loggers = getTodaysLoggers(zoneId);
		if(loggers.isEmpty()){
			return Optional.empty();
		}
		return Optional.of(Rml.paragraph(
				digestService.makeHeading("Logger Configs", paths.datarouter.logging),
				Rml.text("Updated Today").italic(),
				Rml.table(
						Rml.tableRow(
								Rml.tableHeader(Rml.text("Name")),
								Rml.tableHeader(Rml.text("Level")),
								Rml.tableHeader(Rml.text("User")),
								Rml.tableHeader(Rml.text("Updated"))))
						.with(loggers.stream()
								.map(logger -> Rml.tableRow(
										Rml.tableCell(Rml.text(logger.getKey().getName())),
										Rml.tableCell(Rml.text(logger.getLevel().getPersistentString())),
										Rml.tableCell(Rml.text(logger.getEmail())),
										Rml.tableCell(Rml.text(logger.getLastUpdated().format(zoneId))))))));
	}

	private List<LoggerConfig> getTodaysLoggers(ZoneId zoneId){
		return dao.scan()
				.exclude(config -> config.getLastUpdated() == null)
				.exclude(config -> config.getLastUpdated().isBefore(MilliTime.atStartOfDay(zoneId)))
				.list();
	}

}
