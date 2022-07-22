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

import static j2html.TagCreator.b;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.small;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.clustersetting.ClusterSettingValidity;
import io.datarouter.clustersetting.config.DatarouterClusterSettingConfigScanner;
import io.datarouter.clustersetting.config.DatarouterClusterSettingPaths;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSetting;
import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.email.html.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestService;
import io.datarouter.web.html.j2html.J2HtmlTable;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TableTag;

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
	public Optional<DivTag> getPageContent(ZoneId zoneId){
		return makeContent(new ClusterSettingDailyDigestPageFormatter(pageFormatter));
	}

	@Override
	public Optional<DivTag> getEmailContent(ZoneId zoneId){
		return makeContent(new ClusterSettingDailyDigestPageFormatter(emailFormatter));
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.ACTIONABLE;
	}

	private Optional<DivTag> makeContent(ClusterSettingDailyDigestPageFormatter pageFormatter){
		Optional<Pair<String,DivTag>> redundantTable = settingService
				.scanWithValidity(ClusterSettingValidity.REDUNDANT)
				.listTo(settings -> pageFormatter.build(
						settings,
						"Redundant",
						Optional.of("Setting's value in database is the same as the code. Generally safe to delete "
								+ "through the Cluster Setting UI")));
		Optional<Pair<String,DivTag>> unreferencedTable = settingService
				.scanWithValidity(ClusterSettingValidity.UNREFERENCED)
				.listTo(settings -> pageFormatter.build(
						settings,
						"Unreferenced",
						Optional.of("Settings exist in the database but not in the code.")));
		Optional<Pair<String,DivTag>> oldTable = settingService
				.scanWithValidity(ClusterSettingValidity.OLD)
				.listTo(settings -> pageFormatter.build(
						settings,
						"Old",
						Optional.of("Setting has lived in the database for over the threshold. Could update the "
								+ "defaults in the code.")));
		Optional<Pair<String,DivTag>> unknownTable = settingService
				.scanWithValidity(ClusterSettingValidity.UNKNOWN)
				.listTo(settings -> pageFormatter.build(settings, "Unknown", Optional.empty()));

		// Scanners can't figure out types
		List<DivTag> tables = Stream.of(redundantTable, unreferencedTable, oldTable, unknownTable)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.sorted(Comparator.comparing(Pair::getLeft))
				.map(Pair::getRight)
				.collect(Collectors.toList());
		if(tables.size() == 0){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Settings", paths.datarouter.settings.customSettings);
		return Optional.of(div(header, each(tables, tag -> div(tag))));
	}

	private interface ClusterSettingDailyDigestTableFormatter{
		TableTag makeTable(List<ClusterSetting> settings);
	}

	private class ClusterSettingDailyDigestPageFormatter{

		private final ClusterSettingDailyDigestTableFormatter tableFormatter;

		public ClusterSettingDailyDigestPageFormatter(ClusterSettingDailyDigestTableFormatter tableFormatter){
			this.tableFormatter = tableFormatter;
		}

		public Optional<Pair<String,DivTag>> build(
				List<ClusterSetting> settings,
				String header,
				Optional<String> caption){
			if(settings.isEmpty()){
				return Optional.empty();
			}
			return Optional.of(new Pair<>(
					header,
					div(div(b(header)), small(caption.orElse("")), tableFormatter.makeTable(settings))));
		}

	}

	private final ClusterSettingDailyDigestTableFormatter emailFormatter = settings ->
			new J2HtmlEmailTable<ClusterSetting>()
					.withColumn(new J2HtmlEmailTableColumn<>("Name",
							row -> configScanner.makeSettingLink(row.getName())))
					// don't send values in an email
					.build(settings);

	private final ClusterSettingDailyDigestTableFormatter pageFormatter = settings ->
			new J2HtmlTable<ClusterSetting>()
					.withClasses("sortable table table-sm table-striped my-4 border")
					.withHtmlColumn(th("Name").withClass("w-50"),
							row -> td(configScanner.makeSettingLink(row.getName())))
					.withHtmlColumn(th("Value").withClass("w-50"), row -> td(row.getValue()))
					.build(settings);

}
