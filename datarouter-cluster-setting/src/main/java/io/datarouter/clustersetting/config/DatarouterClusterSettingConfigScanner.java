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
package io.datarouter.clustersetting.config;

import static j2html.TagCreator.a;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.clustersetting.ClusterSettingValidity;
import io.datarouter.clustersetting.service.ClusterSettingService;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSetting;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.autoconfig.ConfigScanDto;
import io.datarouter.web.autoconfig.ConfigScanResponseTool;
import io.datarouter.web.email.DatarouterEmailService;
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
		List<String> settings = clusterSettingService.streamWithValidity(ClusterSettingValidity.REDUNDANT)
				.map(ClusterSetting::getName)
				.distinct()
				.list();
		if(settings.isEmpty()){
			return ConfigScanResponseTool.buildEmptyResponse();
		}
		String header = "Found " + settings.size() + " redundant cluster settings";
		return Scanner.of(settings)
				.map(this::makeBrowseSettingLink)
				.listTo(links -> ConfigScanResponseTool.buildResponse(header, links));
	}

	public ConfigScanDto checkForNonexistentClusterSettings(){
		List<String> settings = clusterSettingService.streamWithValidity(ClusterSettingValidity.EXPIRED)
				.map(ClusterSetting::getName)
				.distinct()
				.list();
		if(settings.isEmpty()){
			return ConfigScanResponseTool.buildEmptyResponse();
		}
		String header = "Found " + settings.size() + " nonexistent cluster settings";
		return Scanner.of(settings)
				.map(this::makeBrowseSettingLink)
				.listTo(links -> ConfigScanResponseTool.buildResponse(header, links));
	}

	public ConfigScanDto checkForOldClusterSettings(){
		int oldSettingAlertThresholdDays = clusterSettingsRoot.oldSettingAlertThresholdDays.get();
		List<String> settings = clusterSettingService.streamWithValidity(ClusterSettingValidity.OLD)
				.map(ClusterSetting::getName)
				.distinct()
				.list();
		if(settings.isEmpty()){
			return ConfigScanResponseTool.buildEmptyResponse();
		}
		String header = "Found " + settings.size() + " cluster settings older than " + oldSettingAlertThresholdDays
				+ " days";
		return Scanner.of(settings)
				.map(this::makeBrowseSettingLink)
				.listTo(links -> ConfigScanResponseTool.buildResponse(header, links));
	}

	private ContainerTag makeBrowseSettingLink(String setting){
		String href = emailService.startLinkBuilder()
				.withLocalPath(paths.datarouter.settings)
				.withParam("submitAction", "browseSettings")
				.withParam("name", setting)
				.build();
		return makeLink(setting, href);
	}

	private ContainerTag makeLink(String text, String href){
		return a(text)
				.withHref(href)
				.withStyle("text-decoration:none;");
	}

}
