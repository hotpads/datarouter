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
package io.datarouter.webappinstance.web;

import static j2html.TagCreator.div;
import static j2html.TagCreator.span;
import static j2html.TagCreator.td;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.util.time.ZonedDateFormatterTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.html.pager.Bootstrap4PagerHtml;
import io.datarouter.web.html.pager.MemoryPager;
import io.datarouter.web.html.pager.MemoryPager.Page;
import io.datarouter.web.html.pager.MemorySorter;
import io.datarouter.webappinstance.config.DatarouterWebappInstancePaths;
import io.datarouter.webappinstance.storage.webappinstance.DatarouterWebappInstanceDao;
import io.datarouter.webappinstance.storage.webappinstance.WebappInstanceKey;
import io.datarouter.webappinstance.storage.webappinstancelog.DatarouterWebappInstanceLogDao;
import io.datarouter.webappinstance.storage.webappinstancelog.WebappInstanceLog;
import io.datarouter.webappinstance.storage.webappinstancelog.WebappInstanceLogKey;
import j2html.tags.specialized.DivTag;

public class WebappInstanceHistoryHandler extends BaseHandler{

	@Inject
	private DatarouterWebappInstanceLogDao logDao;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterWebappInstancePaths paths;
	@Inject
	private DatarouterWebappInstanceDao webappInstanceDao;

	// defaultHandler for older MemoryPager with submitAction
	@Handler(defaultHandler = true)
	private Mav history(){
		Scanner<WebappInstanceLogDto> logs = Scanner.of(logDao.scan()
				.groupBy(log -> new WebappInstanceLogKeyDto(
						log.getKey().getServerName(),
						log.getKey().getBuild(),
						log.getBuildId(),
						log.getCommitId(),
						log.getServerPrivateIp()))
				.entrySet())
				.map(entry -> new WebappInstanceLogDto(entry.getKey(), entry.getValue()))
				.sort(Comparator.comparing((WebappInstanceLogDto dto) -> dto.key.buildDate()).reversed());
		MemoryPager<WebappInstanceLogDto> pager = new MemoryPager<>(
				Collections.emptyList(),
				new MemorySorter<>(),
				request.getContextPath() + paths.datarouter.webappInstances.history.toSlashedString(),
				new HashMap<>(),
				params,
				50);
		List<String> activeServerNames = webappInstanceDao.scanKeys()
				.map(WebappInstanceKey::getServerName)
				.list();
		Page<WebappInstanceLogDto> page = pager.collect(logs);
		var content = makeContent(page, activeServerNames);
		return pageFactory.startBuilder(request)
				.withTitle("Running Servers - History")
				.withContent(content)
				.buildMav();
	}

	private DivTag makeContent(Page<WebappInstanceLogDto> page, List<String> activeServerNames){
		var header = WebappInstanceHtml.makeHeader(paths.datarouter.webappInstances.history);
		var form = Bootstrap4PagerHtml.renderForm(page);
		var linkBar = Bootstrap4PagerHtml.renderLinkBar(page)
				.withClass("mt-2");
		ZoneId zoneId = getUserZoneId();
		var table = new J2HtmlTable<WebappInstanceLogDto>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withHtmlColumn("", row -> {
					if(activeServerNames.contains(row.key.serverName)){
						if(row.shutdown.isEmpty()){
							return td();
						}
						if(row.shutdown.get().toEpochMilli() < System.currentTimeMillis()){
							return td();
						}
						return td(span("Active").withClass("badge badge-success"));
					}
					return td();
				})
				.withColumn("Server Name", row -> row.key.serverName)
				.withColumn("Private IP", row -> row.key.serverPrivateIp)
				.withColumn("Build Date", row -> ZonedDateFormatterTool.formatInstantWithZone(row.key.buildDate,
						zoneId))
				.withColumn("Startup Range", row ->
						row.startup
								.map(date -> ZonedDateFormatterTool.formatInstantWithZone(date, zoneId))
								.orElse("unknown")
						+ " - "
						+ row.shutdown
								.map(date -> ZonedDateFormatterTool.formatInstantWithZone(date, zoneId))
								.orElse("unknown"))
				.withColumn("Up Time", row -> {
						if(row.startup.isEmpty() || row.shutdown.isEmpty()){
							return "unknown";
						}
						var duration = Duration.ofMillis(row.shutdown.get().toEpochMilli()
								- row.startup.get().toEpochMilli());
						return new DatarouterDuration(duration).toString(TimeUnit.MINUTES);
				})
				.build(page.rows);
		return div(header, form, linkBar, table)
				.withClass("container-fluid");
	}

	private record WebappInstanceLogKeyDto(
		String serverName,
		Instant buildDate,
		String buildId,
		String commitId,
		String serverPrivateIp){
	}

	private static class WebappInstanceLogDto{

		public final WebappInstanceLogKeyDto key;
		private final Optional<Instant> startup;
		private final Optional<Instant> shutdown;

		public WebappInstanceLogDto(WebappInstanceLogKeyDto key, List<WebappInstanceLog> logRanges){
			this.key = key;
			this.startup = Scanner.of(logRanges)
					.map(WebappInstanceLog::getKey)
					.map(WebappInstanceLogKey::getStartup)
					.sort()
					.findFirst();
			this.shutdown = Scanner.of(logRanges)
					.map(WebappInstanceLog::getRefreshedLast)
					.exclude(Objects::isNull)
					.sort()
					.findLast();
		}

	}

}
