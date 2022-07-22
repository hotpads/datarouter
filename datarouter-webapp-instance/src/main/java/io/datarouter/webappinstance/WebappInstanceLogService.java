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
package io.datarouter.webappinstance;

import static j2html.TagCreator.a;
import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.script;
import static j2html.TagCreator.small;
import static j2html.TagCreator.table;
import static j2html.TagCreator.td;
import static j2html.TagCreator.text;
import static j2html.TagCreator.tr;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.time.ZonedDateFormatterTool;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.J2HtmlTable.J2HtmlTableRow;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import io.datarouter.web.requirejs.RequireJsTool;
import io.datarouter.web.user.session.CurrentUserSessionInfoService;
import j2html.tags.specialized.TdTag;

@Singleton
public class WebappInstanceLogService{

	private static final int LATEST_WEBAPP_INSTANCES = 1;

	public static final String HELPER_FUNCTIONS_SCRIPT = """
			function buildTooltipHtml(commitId, buildId, startupDate, buildDate){
				return '<div><div class="card-header">Build: <b>' + buildId + '</b> / Commit: <b>' + commitId
					+ '</b></div>'
					+ '<div class="card-body" style="white-space: nowrap">'
					+ '<table>'
					+ '<tr><td>startup</td><td><b>' + startupDate.toLocaleString() + '</b></td></tr>'
					+ '<tr><td>built</td><td><b>' + buildDate.toLocaleString() + '</b></td></tr>'
					+ '</table></div></div>';
			}

			function dataTableStartingAfter(startDate){
				const rows = ROWS.filter(row => row[3] > startDate);
				const data = new google.visualization.DataTable();
				data.addColumn('string', 'Role');
				data.addColumn('string', 'Name');
				data.addColumn({type: 'string', role: 'tooltip', p: {html: true}});
				data.addColumn('date', 'Start');
				data.addColumn('date', 'End');
				data.addRows(rows);
				return data;
			}""";

	public static final String CHART_INIT_SCRIPT = """
			let showAll = false;
			const chart = new google.visualization.Timeline(document.getElementById('timeline'));
			const drawGraph = () =>{
				const filterStart = showAll ? new Date(0) : new Date(Date.now() - 7 * 1000 * 60 * 60 * 24);
				chart.draw(dataTableStartingAfter(filterStart),{
					timeline:{ showRowLabels: false, avoidOverlappingGridLines: false }
				});
			};

			drawGraph();
			$('#timeline-toggle').click(function(){
				$(this).blur();
				showAll = !showAll;
				this.innerText = showAll ? 'show past 7 days' : 'show more';
				drawGraph();
			});""";

	@Inject
	private CurrentUserSessionInfoService currentUserSessionInfoService;
	@Inject
	private Bootstrap4PageFactory pageFactory;

	public Mav buildMav(HttpServletRequest request, String webappName, String serverName,
			List<WebappInstanceLogDto> logs){
		setFallbackRefreshedLast(logs);
		ZoneId zoneId = currentUserSessionInfoService.getZoneId(request);
		var table = new J2HtmlTable<WebappInstanceLogDto>()
				.withClasses("sortable table", "table-bordered", "table-sm", "table-striped")
				.withColumn("Build ID", log -> log.buildId)
				.withColumn("Commit ID", log -> log.commitId)
				.withColumn("Private IP", log -> log.serverPrivateIp)
				.withHtmlColumn("Startup Date", log -> toTd(zoneId, log.startup))
				.withHtmlColumn("Build Date", log -> toTd(zoneId, log.build))
				.withColumn("Java Version", log -> log.javaVersion)
				.withColumn("Servlet Container", log -> log.servletContainerVersion)
				.build(logs, log -> {
					var row = new J2HtmlTableRow<>(log);
					Duration duration = Duration.between(log.build, Instant.now());
					if(duration.toDays() < LATEST_WEBAPP_INSTANCES){
						row.withClasses("table-info");
					}
					return row;
				});

		String rowsJsConst = Scanner.of(logs)
				.map(log -> Scanner.of(
						"'ID'",
						"'" + log.buildId + "' || '" + log.commitId + "'",
						"buildTooltipHtml('" + log.commitId + "'"
								+ ", '" + log.buildId + "'"
								+ ", new Date(" + log.startup.toEpochMilli() + ")"
								+ ", new Date(" + log.build.toEpochMilli() + "))",
						"new Date(" + log.startup.toEpochMilli() + ")",
						"new Date(" + log.refreshedLast.toEpochMilli() + ")"))
				.map(list -> list.collect(Collectors.joining(",", "[", "]")))
				.collect(Collectors.joining(",", "const ROWS = [", "];"));

		return pageFactory.startBuilder(request)
				.withTitle("Webapp Instance Log")
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withScript(script(HELPER_FUNCTIONS_SCRIPT))
				.withScript(script(rowsJsConst))
				.withScript(RequireJsTool.makeRequireScriptTagWithCallback(
						new String[]{DatarouterWebRequireJsV2.JQUERY, DatarouterWebRequireJsV2.GOOG + "!timeline"},
						CHART_INIT_SCRIPT))
				.withContent(div().withClasses("container-fluid", "my-4")
						.with(h2()
								.with(small("webapp instance log - ").withClass("text-muted"))
								.with(text(webappName + "/" + serverName)))
						.with(div().withClass("mt-2")
								.with(div().withClass("text-right")
										.with(a("show all")
												.withId("timeline-toggle")
												.withClass("btn-link")
												.withTabindex(0)))
								.with(div().withId("timeline")
										.withStyle("height: 100px")))
						.with(table)
						.with(br())
						.with(table().withClasses("table", "table-bordered", "table-condensed")
								.with(tr(td("Color Codes")))
								.with(tr(td("WebappInstance is within " + LATEST_WEBAPP_INSTANCES + " day"))
										.withClass("table-info"))))
				.buildMav();
	}

	private static void setFallbackRefreshedLast(List<WebappInstanceLogDto> logs){
		for(int i = 0; i < logs.size(); ++i){
			WebappInstanceLogDto log = logs.get(i);
			if(log.refreshedLast == null){
				log.refreshedLast = i == 0 ? Instant.now() : logs.get(i - 1).startup;
			}
		}
	}

	private static TdTag toTd(ZoneId zoneId, Instant instant){
		return td(ZonedDateFormatterTool.formatInstantWithZone(instant, zoneId))
				.attr("sorttable_customkey", instant.toEpochMilli());
	}

	public static class WebappInstanceLogDto{

		private final Instant startup;
		private Instant refreshedLast;
		private final Instant build;
		private final String buildId;
		private final String commitId;
		private final String javaVersion;
		private final String servletContainerVersion;
		private final String serverPrivateIp;

		public WebappInstanceLogDto(Instant startup, Instant refreshedLast, Instant build, String buildId,
				String commitId, String javaVersion, String servletContainerVersion, String serverPrivateIp){
			this.startup = startup;
			this.refreshedLast = refreshedLast;
			this.build = build;
			this.buildId = buildId;
			this.commitId = commitId;
			this.javaVersion = javaVersion;
			this.servletContainerVersion = servletContainerVersion;
			this.serverPrivateIp = serverPrivateIp;
		}

	}

}
