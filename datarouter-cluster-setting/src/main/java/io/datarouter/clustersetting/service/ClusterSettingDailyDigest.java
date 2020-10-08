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
package io.datarouter.clustersetting.service;

import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h3;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.clustersetting.ClusterSettingValidity;
import io.datarouter.clustersetting.config.DatarouterClusterSettingConfigScanner;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSetting;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.html.j2html.J2HtmlTable;
import j2html.TagCreator;
import j2html.tags.ContainerTag;

@Singleton
public class ClusterSettingDailyDigest implements DailyDigest{

	@Inject
	private DatarouterClusterSettingConfigScanner configScanner;
	@Inject
	private ClusterSettingService settingService;

	@Override
	public Optional<ContainerTag> getContent(){
		Optional<ContainerTag> redundantTable = settingService.scanWithValidity(ClusterSettingValidity.REDUNDANT)
				.listTo(settings -> makeBrowserSettingTable(settings, "Redundant"));
		Optional<ContainerTag> expiredTable = settingService.scanWithValidity(ClusterSettingValidity.EXPIRED)
				.listTo(settings -> makeBrowserSettingTable(settings, "Expired"));
		Optional<ContainerTag> oldTable = settingService.scanWithValidity(ClusterSettingValidity.OLD)
				.listTo(settings -> makeBrowserSettingTable(settings, "Old"));
		Optional<ContainerTag> unknownTable = settingService.scanWithValidity(ClusterSettingValidity.UNKNOWN)
				.listTo(settings -> makeBrowserSettingTable(settings, "Unknown"));

		List<ContainerTag> tables = Scanner.of(redundantTable, expiredTable, oldTable, unknownTable)
				.include(Optional::isPresent)
				.map(Optional::get)
				.list();
		if(tables.size() == 0){
			return Optional.empty();
		}
		var header = h3("Settings");
		return Optional.of(div(header, each(tables, TagCreator::div)));
	}

	private Optional<ContainerTag> makeBrowserSettingTable(List<ClusterSetting> settings, String header){
		if(settings.isEmpty()){
			return Optional.empty();
		}
		var table = new J2HtmlTable<ClusterSetting>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withHtmlColumn(th("Name").withClass("w-50"), row -> td(configScanner.makeSettingLink(row.getName())))
				.withHtmlColumn(th("Value").withClass("w-50"), row -> td(row.getValue()))
				.build(settings);
		return Optional.of(div(h4(header), table));
	}

}
