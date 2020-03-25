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
package io.datarouter.nodewatch.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.nodewatch.config.DatarouterNodewatchPaths;
import io.datarouter.nodewatch.storage.alertthreshold.DatarouterTableSizeAlertThresholdDao;
import io.datarouter.nodewatch.storage.alertthreshold.TableSizeAlertThreshold;
import io.datarouter.nodewatch.storage.alertthreshold.TableSizeAlertThresholdKey;
import io.datarouter.nodewatch.storage.tablecount.DatarouterTableCountDao;
import io.datarouter.nodewatch.storage.tablecount.TableCount;
import io.datarouter.nodewatch.util.TableSizeMonitoringEmailBuilder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.DatarouterAdministratorEmailService;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.node.tableconfig.ClientTableEntityPrefixNameWrapper;
import io.datarouter.storage.node.tableconfig.NodewatchConfiguration;
import io.datarouter.storage.node.tableconfig.TableConfigurationService;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.util.DateTool;
import io.datarouter.util.lang.ObjectTool;
import io.datarouter.web.email.DatarouterHtmlEmailService;
import io.datarouter.web.html.email.J2HtmlDatarouterEmailBuilder;
import j2html.tags.ContainerTag;

public class TableSizeMonitoringJob extends BaseJob{

	private static final int IGNORE_THRESHOLD = 100;
	private static final float PERCENTAGE_THRESHOLD = 50;
	private static final int REPORT_DAYS_AFTER = 1;

	@Inject
	private Datarouter datarouter;
	@Inject
	private DatarouterAdministratorEmailService adminEmailService;
	@Inject
	private DatarouterTableSizeAlertThresholdDao tableSizeAlertThresholdDao;
	@Inject
	private DatarouterTableCountDao tableCountDao;
	@Inject
	private DatarouterHtmlEmailService emailService;
	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private TableConfigurationService tableConfigurationService;
	@Inject
	private DatarouterNodewatchPaths paths;
	@Inject
	private DatarouterService datarouterService;
	@Inject
	private TableSizeMonitoringEmailBuilder emailBuilder;

	@Override
	public void run(TaskTracker tracker){
		List<CountStat> aboveThresholdList = new ArrayList<>();
		List<CountStat> abovePercentageList = new ArrayList<>();

		for(PhysicalNode<?,?,?> node : datarouter.getWritableNodes()){
			ClientTableEntityPrefixNameWrapper nodeNames = new ClientTableEntityPrefixNameWrapper(node);
			String tableName = nodeNames.getTableName();
			String clientName = nodeNames.getClientName();
			NodewatchConfiguration nodeConfig = null;
			Long threshold = null;
			boolean enablePercentChangeAlert = true;
			boolean enableThresholdAlert = true;

			nodeConfig = tableConfigurationService.getTableConfigMap().get(nodeNames);
			if(nodeConfig != null){
				threshold = nodeConfig.maxThreshold;
				enablePercentChangeAlert = nodeConfig.enablePercentageAlert;
				enableThresholdAlert = nodeConfig.enableThresholdAlert;
			}

			//continue if the nodeConfig isCountable is set to false
			if(nodeConfig != null && !nodeConfig.isCountable){
				continue;
			}

			List<TableCount> tableCountRecords = tableCountDao.getForTable(clientName, tableName);
			if(tableCountRecords.size() < 2){
				continue;
			}
			Collections.sort(tableCountRecords, new TableCount.TableCountLatestEntryComparator());
			TableCount latest = tableCountRecords.get(0);
			TableCount previous = tableCountRecords.get(1);
			if(previous.getNumRows() == 0){
				continue;
			}
			// skip if the table has records less than the count_threshold
			if(smallEnoughToIgnore(latest.getNumRows()) && smallEnoughToIgnore(previous.getNumRows())){
				continue;
			}

			if(enableThresholdAlert){
				TableSizeAlertThreshold thresholdEntry = tableSizeAlertThresholdDao
						.get(new TableSizeAlertThresholdKey(clientName, tableName));
				// override manual thresholdEntry if exists
				if(thresholdEntry != null && thresholdEntry.getMaxRows() > 0){
					threshold = thresholdEntry.getMaxRows();
				}
				//check if node numRows exceeds threshold
				if(threshold != null && latest.getNumRows() >= threshold){
						aboveThresholdList.add(calculateStats(latest, threshold));
				}
			}

			if(enablePercentChangeAlert){//check % growth if no absolute threshold set & !enablePercentChangeAlert
				CountStat growthIncrease = calculateStats(latest, previous.getNumRows());
				if(growthIncrease == null){
					continue;
				}
				if(Math.abs(growthIncrease.percentageIncrease) > PERCENTAGE_THRESHOLD){
					abovePercentageList.add(growthIncrease);
				}
			}
		}
		List<TableCount> staleList = getStaleTableEntries();
		if(aboveThresholdList.size() > 0
				|| abovePercentageList.size() > 0
				|| staleList.size() > 0){
			sendEmail(aboveThresholdList, abovePercentageList, staleList);
		}
	}

	private boolean checkStaleEntries(TableCount latestSample){
		int ago = DateTool.getDatesBetween(latestSample.getDateUpdated(), new Date());
		return ago > REPORT_DAYS_AFTER;
	}

	private boolean smallEnoughToIgnore(Long numberOfRows){
		return numberOfRows < IGNORE_THRESHOLD;
	}

	private List<TableCount> getStaleTableEntries(){
		Scanner<TableCount> tableCountEntries = tableCountDao.scan();
		Set<TableCount> tableCounts = new HashSet<>();
		TableCount prev = null;
		for(TableCount currEntry : tableCountEntries.iterable()){
			ClientTableEntityPrefixNameWrapper nodeName = new ClientTableEntityPrefixNameWrapper(
					currEntry.getKey().getClientName(), currEntry.getKey().getTableName(), null);
			NodewatchConfiguration nodeConfig = tableConfigurationService.getTableConfigMap().get(nodeName);

			//continue if the nodeConfig isCountable is set to false
			if(nodeConfig != null && !nodeConfig.isCountable){
				continue;
			}
			if(prev == null){
				prev = currEntry;
				continue;
			}
			if(ObjectTool.notEquals(prev.getKey().getClientTableKey(), currEntry.getKey().getClientTableKey())){
				if(checkStaleEntries(prev)){
					tableCounts.add(prev);
				}
			}
			prev = currEntry;
		}
		return new ArrayList<>(tableCounts);
	}

	private CountStat calculateStats(TableCount latestSample, long comparableCount){
		long increase = latestSample.getNumRows() - comparableCount;
		Float percentIncrease = 100F * increase / comparableCount;
		return new CountStat(latestSample, comparableCount, percentIncrease);
	}

	private void sendEmail(List<CountStat> aboveThresholdList, List<CountStat> abovePercentageList,
			List<TableCount> staleList){
		String fromEmail = datarouterProperties.getAdministratorEmail();
		String toEmail = adminEmailService.getAdministratorEmailAddressesCsv();
		String primaryHref = emailService.startLinkBuilder()
				.withLocalPath(paths.datarouter.nodewatch.tableCount)
				.build();
		ContainerTag content = emailBuilder.build(
				datarouterService.getName(),
				datarouterProperties.getServerName(),
				aboveThresholdList,
				PERCENTAGE_THRESHOLD,
				abovePercentageList,
				staleList);
		J2HtmlDatarouterEmailBuilder emailBuilder = emailService.startEmailBuilder()
				.withTitle("Nodewatch")
				.withTitleHref(primaryHref)
				.withContent(content);
		emailService.trySendJ2Html(fromEmail, toEmail, emailBuilder);
	}

	public static class CountStat{

		public final TableCount latestSample;
		public final long previousCount;
		public final float percentageIncrease;
		public final long countDifference;

		public CountStat(TableCount latestSample, long previousCount, Float percentIncrease){
			this.latestSample = latestSample;
			this.previousCount = previousCount;
			this.percentageIncrease = percentIncrease;
			this.countDifference = latestSample.getNumRows() - previousCount;
		}

	}

}
