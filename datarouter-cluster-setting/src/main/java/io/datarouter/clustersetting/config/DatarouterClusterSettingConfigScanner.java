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
		return scanClusterSettingWithValidity(ClusterSettingValidity.REDUNDANT);
	}

	public ConfigScanDto checkForNonexistentClusterSettings(){
		return scanClusterSettingWithValidity(ClusterSettingValidity.EXPIRED);
	}

	public ConfigScanDto checkForInvalidServerTypeClusterSettings(){
		return scanClusterSettingWithValidity(ClusterSettingValidity.INVALID_SERVER_TYPE);
	}

	public ConfigScanDto checkForOldClusterSettings(){
		String additionalMessage = ", older than the alert threshold of "
				+ clusterSettingsRoot.oldSettingAlertThresholdDays.get() + " days";
		return scanClusterSettingWithValidity(ClusterSettingValidity.OLD, additionalMessage);
	}

	private ConfigScanDto scanClusterSettingWithValidity(ClusterSettingValidity validity){
		return scanClusterSettingWithValidity(validity, "");
	}

	private ConfigScanDto scanClusterSettingWithValidity(ClusterSettingValidity validity, String additionalMessage){
		List<String> settings = clusterSettingService.streamWithValidity(validity)
				.map(ClusterSetting::getName)
				.distinct()
				.list();
		if(settings.isEmpty()){
			return ConfigScanResponseTool.buildEmptyResponse();
		}
		int size = settings.size();
		String header = "Found " + size + " " + validity.persistentString + " cluster setting" + (size > 1 ? "s" : "")
				+ (additionalMessage.isEmpty() ? "" : " " + additionalMessage);
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
