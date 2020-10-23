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
package io.datarouter.nodewatch.service;

import static j2html.TagCreator.div;
import static j2html.TagCreator.td;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.nodewatch.config.DatarouterNodewatchPaths;
import io.datarouter.nodewatch.storage.latesttablecount.LatestTableCount;
import io.datarouter.nodewatch.util.TableSizeMonitoringEmailBuilder;
import io.datarouter.util.DateTool;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestService;
import io.datarouter.web.html.email.J2HtmlEmailTable;
import io.datarouter.web.html.email.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import io.datarouter.web.html.j2html.J2HtmlTable;
import j2html.tags.ContainerTag;

@Singleton
public class NodewatchDailyDigest implements DailyDigest{

	@Inject
	private TableSizeMonitoringService monitoringService;
	@Inject
	private TableSizeMonitoringEmailBuilder emailBuilder;
	@Inject
	private DatarouterNodewatchPaths paths;
	@Inject
	private DailyDigestService digestService;

	@Override
	public Optional<ContainerTag> getPageContent(){
		List<LatestTableCount> staleTables = monitoringService.getStaleTableEntries();
		if(staleTables.isEmpty()){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Stale Tables", paths.datarouter.nodewatch.tableCount);
		var table = makePageTable(staleTables);
		return Optional.of(div(header, table));
	}

	@Override
	public Optional<ContainerTag> getEmailContent(){
		List<LatestTableCount> staleTables = monitoringService.getStaleTableEntries();
		if(staleTables.isEmpty()){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Stale Tables", paths.datarouter.nodewatch.tableCount);
		var table = makeEmailTable(staleTables);
		return Optional.of(div(header, table));
	}

	@Override
	public String getTitle(){
		return "Nodewatch";
	}

	@Override
	public DailyDigestGrouping getGrouping(){
		return DailyDigestGrouping.LOW;
	}

	private ContainerTag makePageTable(List<LatestTableCount> staleRows){
		return new J2HtmlTable<LatestTableCount>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withColumn("Client", row -> row.getKey().getClientName())
				.withHtmlColumn("Table", row -> td(emailBuilder.makeTableLink(
						row.getKey().getTableName(),
						row.getKey().getClientName())))
				.withColumn("Latest Count", row -> NumberFormatter.addCommas(row.getNumRows()))
				.withColumn("Date Updated", row -> DateTool.getNumericDate(row.getDateUpdated()))
				.withColumn("UpdatedAgo ago", row -> DateTool.getAgoString(row.getDateUpdated().getTime()))
				.build(staleRows);
	}

	private ContainerTag makeEmailTable(List<LatestTableCount> staleRows){
		return new J2HtmlEmailTable<LatestTableCount>()
				.withColumn("Client", row -> row.getKey().getClientName())
				.withColumn(new J2HtmlEmailTableColumn<>(
						"Table",
						row -> emailBuilder.makeTableLink(row.getKey().getTableName(), row.getKey().getClientName())))
				.withColumn("Latest Count", row -> NumberFormatter.addCommas(row.getNumRows()))
				.withColumn("Date Updated", row -> DateTool.getNumericDate(row.getDateUpdated()))
				.withColumn("UpdatedAgo ago", row -> DateTool.getAgoString(row.getDateUpdated().getTime()))
				.build(staleRows);
	}

}
