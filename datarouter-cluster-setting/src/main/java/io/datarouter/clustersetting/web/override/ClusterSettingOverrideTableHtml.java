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
package io.datarouter.clustersetting.web.override;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h5;
import static j2html.TagCreator.i;
import static j2html.TagCreator.span;
import static j2html.TagCreator.td;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;

import io.datarouter.clustersetting.enums.ClusterSettingOverrideSuggestion;
import io.datarouter.clustersetting.enums.ClusterSettingValidity;
import io.datarouter.clustersetting.service.ClusterSettingService;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSetting;
import io.datarouter.clustersetting.storage.clustersetting.DatarouterClusterSettingDao;
import io.datarouter.clustersetting.web.ClusterSettingHtml;
import io.datarouter.clustersetting.web.browse.ClusterSettingBrowseHandler.ClusterSettingBrowseHandlerParams;
import io.datarouter.clustersetting.web.browse.ClusterSettingBrowseHandler.ClusterSettingBrowseLinks;
import io.datarouter.clustersetting.web.browse.ClusterSettingHierarchy;
import io.datarouter.clustersetting.web.log.ClusterSettingLogHandler.ClusterSettingLogLinks;
import io.datarouter.clustersetting.web.override.handler.ClusterSettingOverrideDeleteHandler.ClusterSettingOverrideDeleteLinks;
import io.datarouter.clustersetting.web.override.handler.ClusterSettingOverrideUpdateHandler.ClusterSettingOverrideUpdateLinks;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.webappinstance.service.WebappInstanceService;
import io.datarouter.webappinstance.storage.webappinstance.DatarouterWebappInstanceDao;
import io.datarouter.webappinstance.storage.webappinstance.WebappInstance;
import io.datarouter.webappinstance.storage.webappinstance.WebappInstanceKey;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TdTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ClusterSettingOverrideTableHtml{

	@Inject
	private ClusterSettingHtml clusterSettingHtml;
	@Inject
	private ClusterSettingOverrideHtml overrideHtml;
	@Inject
	private DatarouterClusterSettingDao dao;
	@Inject
	private ClusterSettingBrowseLinks browseLinks;
	@Inject
	private ClusterSettingLogLinks logLinks;
	@Inject
	private ClusterSettingOverrideUpdateLinks overrideUpdateLinks;
	@Inject
	private ClusterSettingOverrideDeleteLinks overrideDeleteLinks;
	@Inject
	private DatarouterWebappInstanceDao webappInstanceDao;
	@Inject
	private WebappInstanceService webappInstanceService;
	@Inject
	private ClusterSettingService clusterSettingService;
	@Inject
	private ClusterSettingHierarchy clusterSettingHierarchy;

	public DivTag makeTablesDiv(Optional<String> optPartialName, boolean suggestionsOnly){
		WebappInstanceKey currentWebappInstanceKey = webappInstanceService.buildCurrentWebappInstanceKey();
		WebappInstance currentWebappInstance = webappInstanceDao.get(currentWebappInstanceKey);
		// Case-insensitive partialName match
		Predicate<ClusterSetting> partialNamePredicate = setting -> setting.getName().toLowerCase().contains(
				optPartialName.orElse("").toLowerCase());
		if(suggestionsOnly){
			// Create a separate table for each suggestion type
			Map<ClusterSettingOverrideSuggestion,List<ClusterSetting>> settingsBySuggestion = dao.scan()
					.include(partialNamePredicate)
					.groupBy(
							setting -> clusterSettingService.getValidityForWebappInstance(
									setting, currentWebappInstance).overrideSuggestion,
							Function.identity(),
							TreeMap::new);// Follow enum sorting
			if(settingsBySuggestion.isEmpty()){
				return makeNoResults();
			}
			var tablesDiv = div();
			Scanner.of(settingsBySuggestion.entrySet())
					.exclude(entry -> entry.getKey() == ClusterSettingOverrideSuggestion.NOTHING)
					.forEach(entry -> {
						ClusterSettingOverrideSuggestion suggestion = entry.getKey();
						List<ClusterSetting> settings = entry.getValue();
						var tableDiv = div(
								h5("Suggestion: " + suggestion.display),
								makeTableDiv(currentWebappInstance, settings, optPartialName));
						tablesDiv.with(tableDiv);
					});
			return tablesDiv;
		}else{
			// Show everything in one table
			List<ClusterSetting> settings = dao.scan()
					.include(partialNamePredicate)
					.list();
			if(settings.isEmpty()){
				return makeNoResults();
			}
			String headerString = optPartialName
					.map(str -> "Overrides matching: \"" + str + "\"")
					.orElse("All Overrides");
			return div(
					h5(headerString),
					makeTableDiv(currentWebappInstance, settings, optPartialName));
		}
	}

	private DivTag makeTableDiv(
			WebappInstance currentWebappInstance,
			List<ClusterSetting> settings,
			Optional<String> optPartialName){
		boolean showScope = false;// is it necessary?
		var tableBuilder = new J2HtmlTable<ClusterSetting>()
				.withClasses("sortable table table-sm table-striped border");
		tableBuilder.withHtmlColumn(
				"Validity",
				setting -> makeValidityCell(currentWebappInstance, setting));
		tableBuilder.withHtmlColumn(
				"Name",
				this::makeNameCell);
		if(showScope){
			tableBuilder.withColumn(
					"Scope",
					setting -> setting.getScope().display);
		}
		if(anyServerTypeScope(settings)){
			tableBuilder.withColumn(
					"Server Type",
					setting -> ServerType.isUnknownPersistentString(setting.getServerType())
							? ""
							: setting.getServerType());
		}
		if(anyServerNameScope(settings)){
			tableBuilder.withColumn(
					"Server Name",
					ClusterSetting::getServerName);
		}
		tableBuilder.withHtmlColumn(
				"Value",
				this::makeValueCell);
		tableBuilder.withHtmlColumn(
				"Update",
				row -> makeUpdateCell(row, optPartialName));
		tableBuilder.withHtmlColumn(
				"Delete",
				row -> makeDeleteCell(row, optPartialName));
		tableBuilder.withHtmlColumn(
				"Log",
				this::makeLogCell);
		var table = tableBuilder.build(settings);
		return div(table);
	}

	private TdTag makeValidityCell(WebappInstance currentWebappInstance, ClusterSetting setting){
		ClusterSettingValidity validity = clusterSettingService.getValidityForWebappInstance(
				setting,
				currentWebappInstance);
		var td = td(validity.display);
		ClusterSettingOverrideSuggestion suggestion = validity.overrideSuggestion;
		if(suggestion.hasSuggestion){
			String title = String.format("%s. Suggestion: %s", validity.description, suggestion.description);
			td.withTitle(title);
			td.withClass(clusterSettingHtml.overrideSuggestionsTableClass(suggestion));
			var icon = i().withClass("far fa-question-circle");
			td.with(span(icon).withClass("ml-1"));
		}
		return td.withStyle("width:150px;");
	}

	private TdTag makeNameCell(ClusterSetting setting){
		if(clusterSettingHierarchy.settingNamesSorted().contains(setting.getName())){
			String browseHref = browseLinks.all(new ClusterSettingBrowseHandlerParams()
					.withLocation(setting.getName()));
			var link = a(setting.getName()).withHref(browseHref);
			return td(link);
		}
		return td(setting.getName());
	}

	private TdTag makeValueCell(ClusterSetting setting){
		String updateHref = overrideUpdateLinks.update(
				Optional.of(ClusterSettingEditSource.DATABASE),
				Optional.empty(),
				setting.getName(),
				Optional.ofNullable(setting.getServerType()),
				Optional.ofNullable(setting.getServerName()));
		return clusterSettingHtml.makeLimitedLengthLinkCell(setting.getValue(), updateHref)
				.withStyle("width:110px;");
	}

	private TdTag makeUpdateCell(ClusterSetting setting, Optional<String> optPartialName){
		String updateHref = overrideUpdateLinks.update(
				Optional.of(ClusterSettingEditSource.DATABASE),
				optPartialName,
				setting.getName(),
				Optional.ofNullable(setting.getServerType()),
				Optional.ofNullable(setting.getServerName()));
		return td(overrideHtml.makeWarningButtonSmall("Update", updateHref));
	}

	private TdTag makeDeleteCell(ClusterSetting setting, Optional<String> optPartialName){
		String deleteHref = overrideDeleteLinks.delete(
				Optional.of(ClusterSettingEditSource.DATABASE),
				Optional.empty(),
				optPartialName,
				setting.getName(),
				Optional.ofNullable(setting.getServerType()),
				Optional.ofNullable(setting.getServerName()));
		return td(overrideHtml.makeDangerButtonSmall("Delete", deleteHref));
	}

	private TdTag makeLogCell(ClusterSetting setting){
		String logHref = logLinks.setting(setting.getName());
		return td(a("log").withHref(logHref));
	}

	private DivTag makeNoResults(){
		return div(h5("No results found"))
				.withClass("my-5");
	}

	/*-------- helpers ----------*/

	private boolean anyServerTypeScope(List<ClusterSetting> settings){
		return Scanner.of(settings)
				.map(ClusterSetting::getServerType)
				.exclude(ServerType::isUnknownPersistentString)
				.hasAny();
	}

	private boolean anyServerNameScope(List<ClusterSetting> settings){
		return Scanner.of(settings)
				.map(ClusterSetting::getServerName)
				.anyMatch(StringTool::notEmpty);
	}

}
