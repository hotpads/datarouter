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
package io.datarouter.webappinstance.service;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.td;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.email.html.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.time.ZonedDateFormatterTool;
import io.datarouter.util.tuple.Range;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestService;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.webappinstance.config.DatarouterWebappInstancePaths;
import io.datarouter.webappinstance.storage.webappinstancelog.DatarouterWebappInstanceLogDao;
import io.datarouter.webappinstance.storage.webappinstancelog.WebappInstanceLog;
import io.datarouter.webappinstance.storage.webappinstancelog.WebappInstanceLogByBuildInstantKey;
import io.datarouter.webappinstance.storage.webappinstancelog.WebappInstanceLogKey;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TableTag;

@Singleton
public class WebappInstanceDailyDigest implements DailyDigest{

	@Inject
	private DatarouterWebappInstanceLogDao dao;
	@Inject
	private WebappInstanceBuildIdLink buildIdLink;
	@Inject
	private WebappInstanceCommitIdLink commitIdLink;
	@Inject
	private DatarouterWebappInstancePaths paths;
	@Inject
	private DailyDigestService digestService;
	@Inject
	private StandardDeploymentCount standardDeploymentCount;

	@Override
	public Optional<DivTag> getPageContent(ZoneId zoneId){
		var logs = getLogs();
		if(logs.isEmpty()){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Deployments", paths.datarouter.webappInstances.running);
		var table = buildPageTable(logs, zoneId);
		return Optional.of(div(header, table));
	}

	@Override
	public Optional<DivTag> getEmailContent(ZoneId zoneId){
		var logs = getLogs();
		if(logs.isEmpty() || logs.size() <= standardDeploymentCount.getNumberOfStandardDeployments()){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Deployments", paths.datarouter.webappInstances.running);
		var table = buildEmailTable(logs, zoneId);
		return Optional.of(div(header, table));
	}

	@Override
	public String getTitle(){
		return "Deployments";
	}

	@Override
	public DailyDigestGrouping getGrouping(){
		return DailyDigestGrouping.LOW;
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.SUMMARY;
	}

	private List<WebappInstanceLogDto> getLogs(){
		var start = new WebappInstanceLogByBuildInstantKey(startOfDay(), null, null, null);
		var stop = new WebappInstanceLogByBuildInstantKey(endOfDay(), null, null, null);
		var range = new Range<>(start, true, stop, true);
		Map<WebappInstanceLogKeyDto,List<WebappInstanceLog>> ranges = dao.scanDatabeans(range)
				.groupBy(log -> new WebappInstanceLogKeyDto(
						log.getKey().getBuild(),
						log.getBuildId(),
						log.getCommitId()));
		return Scanner.of(ranges.entrySet())
				.map(entry -> new WebappInstanceLogDto(entry.getKey(), entry.getValue()))
				.sort(Comparator.comparing((WebappInstanceLogDto dto) -> dto.key.build))
				.list();
	}

	private TableTag buildPageTable(List<WebappInstanceLogDto> rows, ZoneId zoneId){
		return new J2HtmlTable<WebappInstanceLogDto>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withColumn("Build Date", row -> ZonedDateFormatterTool.formatInstantWithZone(row.key.build, zoneId))
				.withColumn("Startup Range", row -> row.getStartupRangeStart(zoneId)
						+ " - "
						+ row.getStartupRangeEnd(zoneId))
				.withHtmlColumn("BuildId", row -> td(a(Optional.ofNullable(row.key.buildId).orElse(""))
						.withTarget("_blank")
						.withHref(buildIdLink.getLink(Optional.ofNullable(row.key.buildId).orElse("")))))
				.withHtmlColumn("CommitId", row -> td(a(row.key.commitId)
						.withTarget("_blank")
						.withHref(commitIdLink.getLink(row.key.commitId))))
				.build(rows);
	}

	private TableTag buildEmailTable(List<WebappInstanceLogDto> rows, ZoneId zoneId){
		return new J2HtmlEmailTable<WebappInstanceLogDto>()
				.withColumn("Build Date", row -> ZonedDateFormatterTool.formatInstantWithZone(row.key.build, zoneId))
				.withColumn("Startup Range", row ->
						row.getStartupRangeStart(zoneId)
						+ " - "
						+ row.getStartupRangeEnd(zoneId))
				.withColumn(new J2HtmlEmailTableColumn<>(
						"BuildId",
						row -> a(Optional.ofNullable(row.key.buildId).orElse(""))
						.withHref(buildIdLink.getLink(Optional.ofNullable(row.key.buildId).orElse("")))))
				.withColumn(new J2HtmlEmailTableColumn<>(
						"CommitId", row -> a(row.key.commitId).withHref(commitIdLink.getLink(row.key.commitId))))
				.build(rows);
	}

	private static Instant startOfDay(){
		LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
		LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
		return startOfDay.atZone(ZoneId.systemDefault()).toInstant();
	}

	private static Instant endOfDay(){
		LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
		LocalDateTime endOfDay = localDateTime.with(LocalTime.MAX);
		return endOfDay.atZone(ZoneId.systemDefault()).toInstant();
	}

	private record WebappInstanceLogKeyDto(
			Instant build,
			String buildId,
			String commitId){
	}

	private static class WebappInstanceLogDto{

		public final WebappInstanceLogKeyDto key;
		private final List<Instant> startupInstants;

		public WebappInstanceLogDto(WebappInstanceLogKeyDto key, List<WebappInstanceLog> logRanges){
			this.key = key;
			this.startupInstants = Scanner.of(logRanges)
					.map(WebappInstanceLog::getKey)
					.map(WebappInstanceLogKey::getStartup)
					.sort()
					.list();
		}

		public String getStartupRangeStart(ZoneId zoneId){
			return Scanner.of(startupInstants)
					.findFirst()
					.map(date -> ZonedDateFormatterTool.formatInstantWithZone(date, zoneId))
					.get();
		}

		// this should check the refreshed last fields, not the startup dates
		public String getStartupRangeEnd(ZoneId zoneId){
			return ListTool.findLast(startupInstants)
						.map(date -> ZonedDateFormatterTool.formatInstantWithZone(date, zoneId))
						.get();

		}

	}

}
