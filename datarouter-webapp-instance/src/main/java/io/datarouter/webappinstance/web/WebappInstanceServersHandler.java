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
package io.datarouter.webappinstance.web;

import static j2html.TagCreator.div;
import static j2html.TagCreator.span;
import static j2html.TagCreator.td;

import java.time.ZoneId;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.DateTool;
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
import j2html.tags.ContainerTag;

public class WebappInstanceServersHandler extends BaseHandler{

	@Inject
	private DatarouterWebappInstanceLogDao logDao;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterWebappInstancePaths paths;
	@Inject
	private DatarouterWebappInstanceDao webappInstanceDao;
	@Inject
	private DatarouterService datarouterService;

	@Handler(defaultHandler = true)
	private Mav view(){
		Scanner<WebappInstanceLogDto> logs = Scanner.of(logDao.scan().groupBy(WebappInstanceLogKeyDto::new).entrySet())
				.map(entry -> new WebappInstanceLogDto(entry.getKey(), entry.getValue()))
				.sorted(Comparator.comparing((WebappInstanceLogDto dto) -> dto.key.buildDate).reversed());
		MemoryPager<WebappInstanceLogDto> pager = new MemoryPager<>(
				Collections.emptyList(),
				new MemorySorter<>(),
				request.getContextPath() + paths.datarouter.webappInstanceServers.toSlashedString(),
				new HashMap<>(),
				params,
				50);
		List<String> activeServerNames = webappInstanceDao.scanKeys()
				.map(WebappInstanceKey::getServerName)
				.list();
		Page<WebappInstanceLogDto> page = pager.collect(logs);
		var content = makeContent(page, activeServerNames);
		return pageFactory.startBuilder(request)
				.withTitle("Server Startup and Shutdowns")
				.withContent(content)
				.buildMav();
	}

	private ContainerTag makeContent(Page<WebappInstanceLogDto> page, List<String> activeServerNames){
		var form = Bootstrap4PagerHtml.renderForm(page)
				.withClass("mt-4");
		var linkBar = Bootstrap4PagerHtml.renderLinkBar(page)
				.withClass("mt-2");
		ZoneId zoneId = datarouterService.getZoneId();
		var table = new J2HtmlTable<WebappInstanceLogDto>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withHtmlColumn("", row -> {
					if(activeServerNames.contains(row.key.serverName)){
						return td(span("Active").withClass("badge badge-success"));
					}
					return td();
				})
				.withColumn("Server Name", row -> row.key.serverName)
				.withColumn("Build Date", row -> DateTool.formatDateWithZone(row.key.buildDate, zoneId))
				.withColumn("Startup Range", row ->
						row.startupDate
								.map(date -> DateTool.formatDateWithZone(date, zoneId))
								.orElse("unknown")
						+ " - "
						+ row.shutdownDate
								.map(date -> DateTool.formatDateWithZone(date, zoneId))
								.orElse("unknown"))
				.build(page.rows);
		return div(form, linkBar, table)
				.withClass("container-fluid");
	}

	private static class WebappInstanceLogKeyDto{

		public final String serverName;
		public final Date buildDate;
		public final String buildId;
		public final String commitId;

		public WebappInstanceLogKeyDto(WebappInstanceLog log){
			this.serverName = log.getKey().getServerName();
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

			return Objects.equals(this.serverName, that.serverName)
					&& Objects.equals(this.buildDate, that.buildDate)
					&& Objects.equals(this.buildId, that.buildId)
					&& Objects.equals(this.commitId, that.commitId);
		}

		@Override
		public int hashCode(){
			return Objects.hash(serverName, buildDate, buildId, commitId);
		}

	}

	private static class WebappInstanceLogDto{

		public final WebappInstanceLogKeyDto key;
		private final Optional<Date> startupDate;
		private final Optional<Date> shutdownDate;

		public WebappInstanceLogDto(WebappInstanceLogKeyDto key, List<WebappInstanceLog> logRanges){
			this.key = key;
			this.startupDate = Scanner.of(logRanges)
					.map(WebappInstanceLog::getKey)
					.map(WebappInstanceLogKey::getStartupDate)
					.sorted()
					.findFirst();
			this.shutdownDate = Scanner.of(logRanges)
					.map(WebappInstanceLog::getRefreshedLast)
					.exclude(Objects::isNull)
					.sorted()
					.findLast();
		}

	}

}
