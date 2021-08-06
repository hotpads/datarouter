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
package io.datarouter.clustersetting.service;

import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.clustersetting.ClusterSettingValidity;
import io.datarouter.clustersetting.config.DatarouterClusterSettingConfigScanner;
import io.datarouter.clustersetting.config.DatarouterClusterSettingPaths;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSetting;
import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.email.html.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestService;
import io.datarouter.web.html.j2html.J2HtmlTable;
import j2html.TagCreator;
import j2html.tags.ContainerTag;

@Singleton
public class ClusterSettingDailyDigest implements DailyDigest{

	@Inject
	private DatarouterClusterSettingConfigScanner configScanner;
	@Inject
	private ClusterSettingService settingService;
	@Inject
	private DailyDigestService digestService;
	@Inject
	private DatarouterClusterSettingPaths paths;

	@Override
	public String getTitle(){
		return "Cluster Settings";
	}

	@Override
	public DailyDigestGrouping getGrouping(){
		return DailyDigestGrouping.MEDIUM;
	}

	@Override
	public Optional<ContainerTag> getPageContent(ZoneId zoneId){
		return makeContent(new ClusterSettingDailyDigestPageTableFormatter());
	}

	@Override
	public Optional<ContainerTag> getEmailContent(){
		return makeContent(new ClusterSettingDailyDigestEmailTableFormatter());
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.ACTIONABLE;
	}

	private Optional<ContainerTag> makeContent(ClusterSettingDailyDigestTableFormatter tableFormatter){
		var redundantTable = settingService.scanWithValidity(ClusterSettingValidity.REDUNDANT)
				.listTo(settings -> tableFormatter.makeTable(settings, "Redundant"));
		var unreferencedTable = settingService.scanWithValidity(ClusterSettingValidity.UNREFERENCED)
				.listTo(settings -> tableFormatter.makeTable(settings, "Unreferenced"));
		var oldTable = settingService.scanWithValidity(ClusterSettingValidity.OLD)
				.listTo(settings -> tableFormatter.makeTable(settings, "Old"));
		var unknownTable = settingService.scanWithValidity(ClusterSettingValidity.UNKNOWN)
				.listTo(settings -> tableFormatter.makeTable(settings, "Unknown"));

		List<ContainerTag> tables = Scanner.of(redundantTable, unreferencedTable, oldTable, unknownTable)
				.include(Optional::isPresent)
				.map(Optional::get)
				.sort(Comparator.comparing(Pair::getLeft))
				.map(Pair::getRight)
				.list();
		if(tables.size() == 0){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Settings", paths.datarouter.settings, "?submitAction=browseSettings");
		return Optional.of(div(header, each(tables, TagCreator::div)));
	}

	private abstract static class ClusterSettingDailyDigestTableFormatter{
		public abstract Optional<Pair<String,ContainerTag>> makeTable(List<ClusterSetting> settings, String header);
	}

	private class ClusterSettingDailyDigestEmailTableFormatter extends ClusterSettingDailyDigestTableFormatter{
		@Override
		public Optional<Pair<String,ContainerTag>> makeTable(List<ClusterSetting> settings, String header){
			if(settings.isEmpty()){
				return Optional.empty();
			}
			var table = new J2HtmlEmailTable<ClusterSetting>()
					.withColumn(new J2HtmlEmailTableColumn<>("Name",
							row -> configScanner.makeSettingLink(row.getName())))
					// don't send values in an email
					.build(settings);
			return Optional.of(new Pair<>(header, div(h4(header), table)));
		}
	}

	private class ClusterSettingDailyDigestPageTableFormatter extends ClusterSettingDailyDigestTableFormatter{
		@Override
		public Optional<Pair<String,ContainerTag>> makeTable(List<ClusterSetting> settings, String header){
			if(settings.isEmpty()){
				return Optional.empty();
			}
			var table = new J2HtmlTable<ClusterSetting>()
					.withClasses("sortable table table-sm table-striped my-4 border")
					.withHtmlColumn(th("Name").withClass("w-50"),
							row -> td(configScanner.makeSettingLink(row.getName())))
					.withHtmlColumn(th("Value").withClass("w-50"), row -> td(row.getValue()))
					.build(settings);
			return Optional.of(new Pair<>(header, div(h4(header), table)));
		}
	}

}
