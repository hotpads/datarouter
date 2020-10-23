/**
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.tuple.Range;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestService;
import io.datarouter.web.html.email.J2HtmlEmailTable;
import io.datarouter.web.html.email.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.webappinstance.config.DatarouterWebappInstancePaths;
import io.datarouter.webappinstance.storage.webappinstancelog.DatarouterWebappInstanceLogDao;
import io.datarouter.webappinstance.storage.webappinstancelog.WebappInstanceLog;
import io.datarouter.webappinstance.storage.webappinstancelog.WebappInstanceLogByBuildDateKey;
import io.datarouter.webappinstance.storage.webappinstancelog.WebappInstanceLogKey;
import j2html.tags.ContainerTag;

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

	@Override
	public Optional<ContainerTag> getPageContent(){
		var logs = getLogs();
		if(logs.isEmpty()){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Deployments", paths.datarouter.webappInstances);
		var table = buildPageTable(logs);
		return Optional.of(div(header, table));
	}

	@Override
	public Optional<ContainerTag> getEmailContent(){
		var logs = getLogs();
		if(logs.isEmpty()){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Deployments", paths.datarouter.webappInstances);
		var table = buildEmailTable(logs);
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

	private List<WebappInstanceLogDto> getLogs(){
		var start = new WebappInstanceLogByBuildDateKey(startOfDay(), null, null, null);
		var stop = new WebappInstanceLogByBuildDateKey(endOfDay(), null, null, null);
		var range = new Range<>(start, true, stop, true);
		Map<WebappInstanceLogKeyDto,List<WebappInstanceLog>> ranges = dao.scanDatabeans(range)
				.groupBy(WebappInstanceLogKeyDto::new);
		return Scanner.of(ranges.entrySet())
				.map(entry -> new WebappInstanceLogDto(entry.getKey(), entry.getValue()))
				.sorted(Comparator.comparing((WebappInstanceLogDto dto) -> dto.buildDate))
				.list();
	}

	private ContainerTag buildPageTable(List<WebappInstanceLogDto> rows){
		return new J2HtmlTable<WebappInstanceLogDto>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withColumn("Build Date", row -> row.buildDate)
				.withColumn("Startup Range", row -> row.getStartupRangeStart() + " - " + row.getStartupRangeEnd())
				.withHtmlColumn("BuildId", row -> td(a(Optional.ofNullable(row.buildId).orElse(""))
						.withTarget("_blank")
						.withHref(buildIdLink.getLink(Optional.ofNullable(row.buildId).orElse("")))))
				.withHtmlColumn("CommitId", row -> td(a(row.commitId)
						.withTarget("_blank")
						.withHref(commitIdLink.getLink(row.commitId))))
				.build(rows);
	}

	private ContainerTag buildEmailTable(List<WebappInstanceLogDto> rows){
		return new J2HtmlEmailTable<WebappInstanceLogDto>()
				.withColumn("Build Date", row -> row.buildDate)
				.withColumn("Startup Range", row -> row.getStartupRangeStart() + " - " + row.getStartupRangeEnd())
				.withColumn(new J2HtmlEmailTableColumn<>(
						"BuildId",
						row -> a(Optional.ofNullable(row.buildId).orElse(""))
						.withHref(buildIdLink.getLink(Optional.ofNullable(row.buildId).orElse("")))))
				.withColumn(new J2HtmlEmailTableColumn<>(
						"CommitId", row -> a(row.commitId).withHref(commitIdLink.getLink(row.commitId))))
				.build(rows);
	}

	private static Date startOfDay(){
		LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
		LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
		return Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
	}

	private static Date endOfDay(){
		LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
		LocalDateTime endOfDay = localDateTime.with(LocalTime.MAX);
		return Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant());
	}

	private static class WebappInstanceLogKeyDto{

		public final Date buildDate;
		public final String buildId;
		public final String commitId;

		public WebappInstanceLogKeyDto(WebappInstanceLog log){
			this.buildDate = log.getKey().getBuildDate();
			this.buildId = log.getBuildId();
			this.commitId = log.getCommitId();
		}

		@Override
		public boolean equals(Object other){
			if(this == other){
				return true;
			}
			if(!(other instanceof WebappInstanceLogKeyDto)){
				return false;
			}
			WebappInstanceLogKeyDto that = (WebappInstanceLogKeyDto) other;
			return Objects.equals(this.buildDate, that.buildDate)
					&& Objects.equals(this.buildId, that.buildId)
					&& Objects.equals(this.commitId, that.commitId);
		}

		@Override
		public int hashCode(){
			return Objects.hash(buildDate, buildId, commitId);
		}

	}

	private static class WebappInstanceLogDto{

		public final Date buildDate;
		public final String buildId;
		public final String commitId;
		private final List<Date> startupDates;

		public WebappInstanceLogDto(WebappInstanceLogKeyDto key, List<WebappInstanceLog> logRanges){
			this.buildDate = key.buildDate;
			this.buildId = key.buildId;
			this.commitId = key.commitId;
			this.startupDates = Scanner.of(logRanges)
					.map(WebappInstanceLog::getKey)
					.map(WebappInstanceLogKey::getStartupDate)
					.sorted()
					.list();
		}

		public String getStartupRangeStart(){
			return Scanner.of(startupDates)
					.findFirst()
					.map(Date::toString)
					.get();
		}

		public String getStartupRangeEnd(){
			return ListTool.findLast(startupDates)
					.map(Date::toString)
					.get();
		}

	}

}
