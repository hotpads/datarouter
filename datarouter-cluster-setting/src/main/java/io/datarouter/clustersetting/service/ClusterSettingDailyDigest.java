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
import static j2html.TagCreator.small;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import io.datarouter.clustersetting.config.DatarouterClusterSettingPaths;
import io.datarouter.clustersetting.enums.ClusterSettingValidity;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSetting;
import io.datarouter.clustersetting.web.ClusterSettingHtml;
import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.email.html.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import io.datarouter.scanner.OptionalScanner;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestService;
import io.datarouter.web.html.j2html.J2HtmlTable;
import j2html.TagCreator;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TableTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ClusterSettingDailyDigest implements DailyDigest{

	@Inject
	private ClusterSettingService settingService;
	@Inject
	private DailyDigestService digestService;
	@Inject
	private DatarouterClusterSettingPaths paths;
	@Inject
	private ClusterSettingHtml clusterSettingHtml;

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
		return makeContent(
				new ClusterSettingDailyDigestPageFormatter(pageFormatter),
				new ClusterSettingDailyDigestPageFormatter(pageFormatter));
	}

	@Override
	public Optional<DivTag> getEmailContent(ZoneId zoneId){
		return makeContent(
				new ClusterSettingDailyDigestPageFormatter(emailFormatter),
				new ClusterSettingDailyDigestPageFormatter(emailPrefixFormatter));
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.ACTIONABLE;
	}

	private Optional<DivTag> makeContent(
			ClusterSettingDailyDigestPageFormatter settingFormatter,
			ClusterSettingDailyDigestPageFormatter prefixFormatter){
		Optional<HeaderAndContent> redundantTable = settingService
				.scanWithValidity(ClusterSettingValidity.REDUNDANT)
				.listTo(settings -> settingFormatter.build(
						settings,
						"Redundant",
						Optional.of("Setting's value in database is the same as the code. Generally safe to delete "
								+ "through the Cluster Setting UI")));
		Optional<HeaderAndContent> unreferencedTable = settingService
				.scanWithValidity(ClusterSettingValidity.UNREFERENCED)
				.listTo(settings -> settingFormatter.build(
						settings,
						"Unreferenced",
						Optional.of("Settings exist in the database but not in the code.")));
		Optional<HeaderAndContent> oldTable = settingService
				.scanWithValidity(ClusterSettingValidity.OLD)
				.listTo(settings -> settingFormatter.build(
						settings,
						"Old",
						Optional.of("Setting has lived in the database for over the threshold. Could update the "
								+ "defaults in the code.")));
		Optional<HeaderAndContent> unknownTable = settingService
				.scanWithValidity(ClusterSettingValidity.UNKNOWN)
				.listTo(settings -> prefixFormatter.build(settings, "Unknown", Optional.empty()));

		List<DivTag> tables = Scanner.of(redundantTable, unreferencedTable, oldTable, unknownTable)
				.concat(OptionalScanner::of)
				.sort(Comparator.comparing(HeaderAndContent::header))
				.map(HeaderAndContent::content)
				.list();
		if(tables.size() == 0){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Settings", paths.datarouter.settings.overrides.view);
		var tablesDiv = div(header);
		Scanner.of(tables)
				.map(TagCreator::div)
				.forEach(tablesDiv::with);
		return Optional.of(tablesDiv);
	}

	private interface ClusterSettingDailyDigestTableFormatter{
		TableTag makeTable(List<ClusterSetting> settings);
	}

	private class ClusterSettingDailyDigestPageFormatter{

		private final ClusterSettingDailyDigestTableFormatter tableFormatter;

		public ClusterSettingDailyDigestPageFormatter(ClusterSettingDailyDigestTableFormatter tableFormatter){
			this.tableFormatter = tableFormatter;
		}

		public Optional<HeaderAndContent> build(
				List<ClusterSetting> settings,
				String header,
				Optional<String> caption){
			if(settings.isEmpty()){
				return Optional.empty();
			}
			return Optional.of(new HeaderAndContent(
					header,
					div(div(b(header)), small(caption.orElse("")), tableFormatter.makeTable(settings))));
		}

	}

	private record HeaderAndContent(
			String header,
			DivTag content){
	}

	private final ClusterSettingDailyDigestTableFormatter emailFormatter = settings ->
			new J2HtmlEmailTable<ClusterSetting>()
					.withColumn(new J2HtmlEmailTableColumn<>("Name",
							row -> clusterSettingHtml.makeBrowseSettingLink(row.getName())))
					// don't send values in an email
					.build(settings);

	private final ClusterSettingDailyDigestTableFormatter emailPrefixFormatter = settings ->
			new J2HtmlEmailTable<ClusterSetting>()
					.withColumn(new J2HtmlEmailTableColumn<>("Name",
							row -> clusterSettingHtml.makeOverridePrefixSettingLink(row.getName())))
					// don't send values in an email
					.build(settings);

	private final ClusterSettingDailyDigestTableFormatter pageFormatter = settings ->
			new J2HtmlTable<ClusterSetting>()
					.withClasses("sortable table table-sm table-striped my-4 border")
					.withHtmlColumn(th("Name").withClass("w-50"),
							row -> td(clusterSettingHtml.makeBrowseSettingLink(row.getName())))
					.withHtmlColumn(th("Value").withClass("w-50"), row -> td(row.getValue()))
					.build(settings);

}
