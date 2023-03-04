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
package io.datarouter.nodewatch.web;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDtoBuilder;
import io.datarouter.joblet.service.JobletService;
import io.datarouter.nodewatch.config.DatarouterNodewatchFiles;
import io.datarouter.nodewatch.config.DatarouterNodewatchPaths;
import io.datarouter.nodewatch.joblet.TableSpanSamplerJoblet;
import io.datarouter.nodewatch.joblet.TableSpanSamplerJobletCreatorFactory;
import io.datarouter.nodewatch.service.TableSamplerService;
import io.datarouter.nodewatch.storage.latesttablecount.DatarouterLatestTableCountDao;
import io.datarouter.nodewatch.storage.latesttablecount.LatestTableCount;
import io.datarouter.nodewatch.storage.latesttablecount.LatestTableCountKey;
import io.datarouter.nodewatch.storage.tablecount.DatarouterTableCountDao;
import io.datarouter.nodewatch.storage.tablecount.TableCount;
import io.datarouter.nodewatch.storage.tablecount.TableCount.TableCountLatestEntryComparator;
import io.datarouter.nodewatch.storage.tablecount.TableCountKey;
import io.datarouter.nodewatch.storage.tablesample.DatarouterTableSampleDao;
import io.datarouter.nodewatch.storage.tablesample.TableSampleKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.NodeTool;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader.PhysicalSortedStorageReaderNode;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.util.time.ZonedDateFormatterTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.InContextRedirectMav;
import io.datarouter.web.handler.mav.imp.MessageMav;

public class TableCountHandler extends BaseHandler{

	@Inject
	private DatarouterTableCountDao tableCountDao;
	@Inject
	private DatarouterTableSampleDao tableSampleDao;
	@Inject
	private DatarouterNodes nodes;
	@Inject
	private JobletService jobletService;
	@Inject
	private DatarouterLatestTableCountDao latestTableCountDao;
	@Inject
	private TableSpanSamplerJobletCreatorFactory tableSpanSamplerJobletCreatorFactory;
	@Inject
	private DatarouterNodewatchFiles files;
	@Inject
	private DatarouterNodewatchPaths paths;
	@Inject
	private TableSamplerService tableSamplerService;
	@Inject
	private DatarouterNodes datarouterNodes;
	@Inject
	private ChangelogRecorder changelogRecorder;

	@Handler(defaultHandler = true)
	private Mav latestTableCounts(){
		ZoneId userZoneId = getUserZoneId();
		Mav mav = new Mav(files.jsp.datarouter.nodewatch.latestTableCountsJsp);
		Map<String,List<TableCountJspDto>> latestTableCountDtoMap = latestTableCountDao.scan()
				.splitBy(count -> count.getKey().getClientName())
				.concat(counts -> counts.sort(Comparator.comparing(LatestTableCount::getNumRows).reversed()))
				.map(count -> new TableCountJspDto(count, userZoneId))
				.groupBy(dto -> dto.clientName);

		mav.put("latestTableCountDtoMap", latestTableCountDtoMap);
		return mav;
	}

	@Handler
	private Mav singleTableWithNodeName(String nodeName){
		PhysicalNode<?,?,?> node = NodeTool.extractSinglePhysicalNode(datarouterNodes.getNode(nodeName));
		String clientName = node.getFieldInfo().getClientId().getName();
		String tableName = node.getFieldInfo().getTableName();
		return singleTable(clientName, tableName);
	}

	@Handler
	private Mav singleTable(String clientName, String tableName){
		ZoneId userZoneId = getUserZoneId();
		Mav mav = new Mav(files.jsp.datarouter.nodewatch.singleTableCountsJsp);
		mav.put("clientName", clientName);
		mav.put("tableName", tableName);
		List<TableCount> results = tableCountDao.scanForTable(clientName, tableName)
				.sort(new TableCountLatestEntryComparator())
				.list();
		mav.put("results", Scanner.of(results)
				.map(count -> new TableCountJspDto(count, userZoneId))
				.list());
		mav.put("jsonData", getRowCountData(clientName, tableName));
		return mav;
	}

	@Handler
	private TableCountJspDto recount(String clientName, String tableName){
		TableCount recount = tableSamplerService.getCurrentTableCountFromSamples(clientName, tableName);
		return new TableCountJspDto(recount, getUserZoneId());
	}

	@Handler
	private Mav resample(String clientName, String tableName){
		var node = (PhysicalSortedStorageReaderNode<?,?,?>)nodes.getPhysicalNodeForClientAndTable(clientName,
				tableName);
		tableSpanSamplerJobletCreatorFactory.create(
				node,
				tableSamplerService.getSampleInterval(node),
				tableSamplerService.getBatchSize(node),
				true,
				true,
				System.currentTimeMillis())
				.createJoblets();
		var dto = new DatarouterChangelogDtoBuilder(
				"Nodewatch",
				clientName + "." + tableName,
				"resample",
				getSessionInfo().getNonEmptyUsernameOrElse(""))
				.build();
		changelogRecorder.record(dto);
		return new InContextRedirectMav(request, paths.datarouter.nodewatch.tableCount.toSlashedString()
				+ "?submitAction=singleTable&clientName=" + clientName + "&tableName=" + tableName);
	}

	@Handler
	private Mav deleteAllMetadata(String clientName, String tableName){
		//delete rows from TableCount
		var tableCountKeyPrefix = new TableCountKey(clientName, tableName, null);
		tableCountDao.deleteWithPrefix(tableCountKeyPrefix);

		//delete rows from TableSample
		var tableSampleKeyPrefix = new TableSampleKey(clientName, tableName, null, null);
		tableSampleDao.deleteWithPrefix(tableSampleKeyPrefix);

		//delete from LatestTableCount
		var latestTableCountKey = new LatestTableCountKey(clientName, tableName);
		latestTableCountDao.delete(latestTableCountKey);

		var dto = new DatarouterChangelogDtoBuilder(
				"Nodewatch",
				clientName + "." + tableName,
				"deleted metadata",
				getSessionInfo().getNonEmptyUsernameOrElse(""))
				.build();
		changelogRecorder.record(dto);
		return new InContextRedirectMav(request, paths.datarouter.nodewatch.tableCount.toSlashedString());
	}

	@Handler
	private Mav deleteRowSamples(String clientName, String tableName){
		//delete rows from TableSample
		var tableSampleKeyPrefix = new TableSampleKey(clientName, tableName, null, null);
		tableSampleDao.deleteWithPrefix(tableSampleKeyPrefix);
		var dto = new DatarouterChangelogDtoBuilder(
				"Nodewatch",
				clientName + "." + tableName,
				"deleted row samples",
				getSessionInfo().getNonEmptyUsernameOrElse(""))
				.build();
		changelogRecorder.record(dto);
		return new InContextRedirectMav(request, paths.datarouter.nodewatch.tableCount.toSlashedString());
	}

	@Handler
	private Mav unscheduleSamples(){
		jobletService.deleteJobletsOfType(TableSpanSamplerJoblet.JOBLET_TYPE);
		tableSampleDao.resetSchedules();
		return new MessageMav("complete");
	}

	private JsonArray getRowCountData(String clientName, String tableName){
		List<TableCount> data = tableCountDao.scanForTable(clientName, tableName)
				.list();
		JsonArray jsonData = getRowCountJson(data);
		return jsonData;
	}

	private JsonArray getRowCountJson(List<TableCount> records){
		var jsonArray = new JsonArray();
		for(TableCount record : records){
			JsonObject json = new JsonObject();
			Long date = record.getDateUpdated().toEpochMilli();
			Long rows = record.getNumRows();
			json.addProperty("date", date);
			json.addProperty("rows", rows);
			jsonArray.add(json);
		}
		return jsonArray;
	}

	public static class TableCountJspDto{

		private final String clientName;
		private final String tableName;
		private final Long numRows;
		private final String countTime;
		private final Long countTimeMs;
		private final String dateUpdated;
		private final String dateCreated;
		private final Long dateCreatedTime;
		private final Long numSpans;
		private final Long numSlowSpans;

		public TableCountJspDto(LatestTableCount latestTableCount, ZoneId zoneId){
			this(latestTableCount.getKey().getClientName(),
					latestTableCount.getKey().getTableName(),
					latestTableCount.getNumRows(),
					latestTableCount.getCountTimeMs(),
					latestTableCount.getDateUpdated(),
					null,
					latestTableCount.getNumSpans(),
					latestTableCount.getNumSlowSpans(),
					zoneId);
		}

		public TableCountJspDto(TableCount tableCount, ZoneId zoneId){
			this(tableCount.getKey().getClientName(),
					tableCount.getKey().getTableName(),
					tableCount.getNumRows(),
					tableCount.getCountTimeMs(),
					tableCount.getDateUpdated(),
					tableCount.getKey().getCreatedMs(),
					tableCount.getNumSpans(),
					tableCount.getNumSlowSpans(),
					zoneId);
		}

		public TableCountJspDto(
				String clientName,
				String tableName,
				Long numRows,
				Long countTimeMs,
				Instant dateUpdated,
				Long dateCreatedTime,
				Long numSpans,
				Long numSlowSpans,
				ZoneId zoneId){
			this.clientName = clientName;
			this.tableName = tableName;
			this.numRows = numRows;
			this.countTime = new DatarouterDuration(countTimeMs, TimeUnit.MILLISECONDS).toString();
			this.countTimeMs = countTimeMs;
			this.dateUpdated = ZonedDateFormatterTool.formatInstantWithZone(dateUpdated, zoneId);
			this.dateCreated = dateCreatedTime == null
					? null
					: ZonedDateFormatterTool.formatLongMsWithZone(dateCreatedTime, zoneId);
			this.dateCreatedTime = dateCreatedTime;
			this.numSpans = numSpans;
			this.numSlowSpans = numSlowSpans;
		}

		public String getDateCreated(){
			return dateCreated;
		}

		public Long getDateCreatedTime(){
			return dateCreatedTime;
		}

		public String getClientName(){
			return clientName;
		}

		public String getTableName(){
			return tableName;
		}

		public Long getNumRows(){
			return numRows;
		}

		public String getCountTime(){
			return countTime;
		}

		public Long getCountTimeMs(){
			return countTimeMs;
		}

		public String getDateUpdated(){
			return dateUpdated;
		}

		public Long getnumSpans(){
			return numSpans;
		}

		public Long getnumSlowSpans(){
			return numSlowSpans;
		}

	}

}
