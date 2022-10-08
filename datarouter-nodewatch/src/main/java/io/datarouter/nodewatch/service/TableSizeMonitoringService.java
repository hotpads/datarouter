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
package io.datarouter.nodewatch.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.email.html.J2HtmlDatarouterEmailBuilder;
import io.datarouter.email.type.DatarouterEmailTypes.NodewatchEmailType;
import io.datarouter.nodewatch.config.DatarouterNodewatchPaths;
import io.datarouter.nodewatch.storage.alertthreshold.DatarouterTableSizeAlertThresholdDao;
import io.datarouter.nodewatch.storage.alertthreshold.TableSizeAlertThreshold;
import io.datarouter.nodewatch.storage.alertthreshold.TableSizeAlertThresholdKey;
import io.datarouter.nodewatch.storage.latesttablecount.DatarouterLatestTableCountDao;
import io.datarouter.nodewatch.storage.latesttablecount.LatestTableCount;
import io.datarouter.nodewatch.storage.tablecount.DatarouterTableCountDao;
import io.datarouter.nodewatch.storage.tablecount.TableCount;
import io.datarouter.nodewatch.util.TableSizeMonitoringEmailBuilder;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.tableconfig.ClientTableEntityPrefixNameWrapper;
import io.datarouter.storage.node.tableconfig.NodewatchConfiguration;
import io.datarouter.storage.node.tableconfig.TableConfigurationService;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.web.email.DatarouterHtmlEmailService;
import j2html.tags.specialized.BodyTag;

@Singleton
public class TableSizeMonitoringService{

	private static final int IGNORE_THRESHOLD = 100;
	public static final float PERCENTAGE_THRESHOLD = 50;
	private static final Duration REPORT_AFTER = Duration.ofDays(1);

	@Inject
	private DatarouterNodes datarouterNodes;
	@Inject
	private DatarouterClients clients;
	@Inject
	private NodewatchEmailType nodewatchEmailType;
	@Inject
	private DatarouterTableSizeAlertThresholdDao tableSizeAlertThresholdDao;
	@Inject
	private DatarouterTableCountDao tableCountDao;
	@Inject
	private DatarouterHtmlEmailService emailService;
	@Inject
	private TableConfigurationService tableConfigurationService;
	@Inject
	private DatarouterNodewatchPaths paths;
	@Inject
	private TableSizeMonitoringEmailBuilder emailBuilder;
	@Inject
	private DatarouterLatestTableCountDao datarouterLatestTableCountDao;

	public void run(){
		AboveThreshold dto = getAboveThresholdLists();
		List<ThresholdCountStat> aboveThresholdList = dto.aboveThreshold();
		List<PercentageCountStat> abovePercentageList = dto.abovePercentage();
		List<LatestTableCount> staleList = getStaleTableEntries();
		if(aboveThresholdList.size() > 0
				|| abovePercentageList.size() > 0
				|| staleList.size() > 0){
			sendEmail(aboveThresholdList, abovePercentageList, staleList);
		}
	}

	public AboveThreshold getAboveThresholdLists(){
		List<ThresholdCountStat> aboveThresholdList = new ArrayList<>();
		List<PercentageCountStat> abovePercentageList = new ArrayList<>();

		for(PhysicalNode<?,?,?> node : datarouterNodes.getWritableNodes(clients.getClientIds())){
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
				Optional<TableSizeAlertThreshold> thresholdEntry = tableSizeAlertThresholdDao
						.find(new TableSizeAlertThresholdKey(clientName, tableName));
				// override manual thresholdEntry if exists
				if(thresholdEntry.isPresent() && thresholdEntry.get().getMaxRows() > 0){
					threshold = thresholdEntry.get().getMaxRows();
				}
				//check if node numRows exceeds threshold
				if(threshold != null && latest.getNumRows() >= threshold){
					aboveThresholdList.add(new ThresholdCountStat(latest, threshold));
				}
			}

			if(enablePercentChangeAlert){//check % growth if no absolute threshold set & !enablePercentChangeAlert
				PercentageCountStat growthIncrease = calculatePercentageStats(latest, previous.getNumRows());
				if(growthIncrease == null){
					continue;
				}
				if(Math.abs(growthIncrease.percentageIncrease) > PERCENTAGE_THRESHOLD){
					abovePercentageList.add(growthIncrease);
				}
			}
		}
		return new AboveThreshold(aboveThresholdList, abovePercentageList);
	}

	private boolean checkStaleEntries(LatestTableCount latestSample){
		Duration age = Duration.between(latestSample.getDateUpdated(), Instant.now());
		return age.compareTo(REPORT_AFTER) > 0;
	}

	private boolean smallEnoughToIgnore(Long numberOfRows){
		return numberOfRows < IGNORE_THRESHOLD;
	}

	public List<LatestTableCount> getStaleTableEntries(){
		return datarouterLatestTableCountDao.scan()
				.exclude(latestTableCount -> {
					ClientTableEntityPrefixNameWrapper nodeName = new ClientTableEntityPrefixNameWrapper(
							latestTableCount.getKey().getClientName(),
							latestTableCount.getKey().getTableName(),
							null);
					NodewatchConfiguration nodeConfig = tableConfigurationService.getTableConfigMap().get(nodeName);
					return nodeConfig != null && !nodeConfig.isCountable;
				})
				.include(this::checkStaleEntries)
				.list();
	}

	private PercentageCountStat calculatePercentageStats(TableCount latestSample, long comparableCount){
		long increase = latestSample.getNumRows() - comparableCount;
		Float percentIncrease = 100F * increase / comparableCount;
		return new PercentageCountStat(latestSample, comparableCount, percentIncrease);
	}

	private void sendEmail(
			List<ThresholdCountStat> aboveThresholdList,
			List<PercentageCountStat> abovePercentageList,
			List<LatestTableCount> staleList){
		String primaryHref = emailService.startLinkBuilder()
				.withLocalPath(paths.datarouter.nodewatch.tableCount)
				.build();
		BodyTag content = emailBuilder.build(
				aboveThresholdList,
				PERCENTAGE_THRESHOLD,
				abovePercentageList,
				staleList);
		J2HtmlDatarouterEmailBuilder emailBuilder = emailService.startEmailBuilder()
				.withTitle("Nodewatch")
				.withTitleHref(primaryHref)
				.withContent(content)
				.fromAdmin()
				.to(nodewatchEmailType.tos);
		emailService.trySendJ2Html(emailBuilder);
	}

	public record AboveThreshold(
			List<ThresholdCountStat> aboveThreshold,
			List<PercentageCountStat> abovePercentage){
	}

	public static class PercentageCountStat{
		public final TableCount latestSample;
		public final long previousCount;
		public final float percentageIncrease;
		public final long countDifference;

	public PercentageCountStat(TableCount latestSample, long previousCount, Float percentIncrease){
				this.latestSample = latestSample;
				this.previousCount = previousCount;
				this.percentageIncrease = percentIncrease;
				this.countDifference = latestSample.getNumRows() - previousCount;
			}
	}

	public record ThresholdCountStat(
			TableCount latestSample,
			long threshold){
	}

}
