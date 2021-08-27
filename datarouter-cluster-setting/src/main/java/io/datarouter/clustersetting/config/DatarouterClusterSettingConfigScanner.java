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
package io.datarouter.clustersetting.config;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h4;

import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.clustersetting.ClusterSettingScopeComparator;
import io.datarouter.clustersetting.ClusterSettingValidity;
import io.datarouter.clustersetting.service.ClusterSettingService;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSetting;
import io.datarouter.email.email.DatarouterEmailService;
import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.email.html.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.autoconfig.ConfigScanDto;
import io.datarouter.web.autoconfig.ConfigScanResponseTool;
import j2html.tags.ContainerTag;

@Singleton
public class DatarouterClusterSettingConfigScanner{

	@Inject
	private ClusterSettingService clusterSettingService;
	@Inject
	private DatarouterClusterSettingRoot clusterSettingsRoot;
	@Inject
	private DatarouterEmailService emailService;
	@Inject
	private DatarouterClusterSettingPaths paths;

	public ConfigScanDto checkForRedundantClusterSettings(){
		return buildConfigScanResponse(
				clusterSettingService.scanAllWebappInstancesWithRedundantValidity(),
				ClusterSettingValidity.REDUNDANT,
				", redundant on all webapp instances");
	}

	public ConfigScanDto checkForNonexistentClusterSettings(){
		return scanClusterSettingWithValidity(ClusterSettingValidity.UNREFERENCED, "");
	}

	public ConfigScanDto checkForInvalidServerTypeClusterSettings(){
		return scanClusterSettingWithValidity(ClusterSettingValidity.INVALID_SERVER_TYPE, "");
	}

	public ConfigScanDto checkForOldClusterSettings(){
		String additionalMessage = ", older than the alert threshold of "
				+ clusterSettingsRoot.oldSettingAlertThresholdDays.get() + " days";
		return scanClusterSettingWithValidity(ClusterSettingValidity.OLD, additionalMessage);
	}

	private ConfigScanDto scanClusterSettingWithValidity(ClusterSettingValidity validity, String additionalMessage){
		return buildConfigScanResponse(clusterSettingService.scanWithValidity(validity), validity, additionalMessage);
	}

	private ConfigScanDto buildConfigScanResponse(
			Scanner<ClusterSetting> settingsScanner,
			ClusterSettingValidity validity,
			String additionalMessage){
		List<ClusterSetting> clusterSettings = settingsScanner
				.distinctBy(BaseDatabean::getKey)
				.sort(Comparator.comparing(ClusterSetting::getName)
						.thenComparing(new ClusterSettingScopeComparator().reversed()))
				.list();
		if(clusterSettings.isEmpty()){
			return ConfigScanResponseTool.buildEmptyResponse();
		}
		int size = clusterSettings.size();
		String header = "Found " + size + " " + validity.persistentString + " cluster setting" + (size > 1 ? "s" : "")
				+ (additionalMessage.isEmpty() ? "" : additionalMessage);
		ContainerTag<?> settingsTable = makeBrowserSettingTable(clusterSettings, header);
		return ConfigScanResponseTool.buildResponse(settingsTable);
	}

	private ContainerTag<?> makeBrowserSettingTable(List<ClusterSetting> settings, String header){
		var table = new J2HtmlEmailTable<ClusterSetting>()
				.withColumn(new J2HtmlEmailTableColumn<>("Name", row -> makeSettingLink(row.getName())))
				.withColumn("Scope", row -> row.getScope().getPersistentString())
				.withColumn("Server Type", ClusterSetting::getServerType)
				.withColumn("Server Name", ClusterSetting::getServerName)
				.withColumn("Value", row -> {
					if(clusterSettingsRoot.isExcludedOldSettingString(row.getName())){
						return "********";
					}
					return row.getValue();
				})
				.build(settings);
		return div(h4(header), table);
	}

	public ContainerTag<?> makeSettingLink(String settingName){
		String href = emailService.startLinkBuilder()
				.withLocalPath(paths.datarouter.settings)
				.withParam("submitAction", "browseSettings")
				.withParam("name", settingName)
				.build();
		return a(settingName)
				.withHref(href)
				.withStyle("text-decoration:none;");
	}

}
