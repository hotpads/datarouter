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
package io.datarouter.clustersetting.web.browse.setting;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.span;
import static j2html.TagCreator.strong;
import static j2html.TagCreator.td;

import java.util.List;
import java.util.Optional;

import io.datarouter.clustersetting.enums.ClusterSettingScope;
import io.datarouter.clustersetting.service.ClusterSettingService;
import io.datarouter.clustersetting.storage.clustersetting.ClusterSetting;
import io.datarouter.clustersetting.storage.clustersetting.DatarouterClusterSettingDao;
import io.datarouter.clustersetting.storage.clustersettinglog.DatarouterClusterSettingLogDao;
import io.datarouter.clustersetting.web.browse.ClusterSettingBrowseHandler.ClusterSettingBrowseHandlerParams;
import io.datarouter.clustersetting.web.browse.ClusterSettingBrowseHandler.ClusterSettingBrowseLinks;
import io.datarouter.clustersetting.web.browse.setting.CodeOverridesTool.CodeOverrideRow;
import io.datarouter.clustersetting.web.browse.setting.DatabaseOverridesTool.DatabaseOverrideRow;
import io.datarouter.clustersetting.web.browse.setting.TagOverridesTool.TagOverrideRow;
import io.datarouter.clustersetting.web.log.ClusterSettingLogHandler.ClusterSettingLogLinks;
import io.datarouter.clustersetting.web.override.ClusterSettingEditSource;
import io.datarouter.clustersetting.web.override.handler.ClusterSettingOverrideCreateHandler.ClusterSettingOverrideCreateLinks;
import io.datarouter.clustersetting.web.override.handler.ClusterSettingOverrideDeleteHandler.ClusterSettingOverrideDeleteLinks;
import io.datarouter.clustersetting.web.override.handler.ClusterSettingOverrideUpdateHandler.ClusterSettingOverrideUpdateLinks;
import io.datarouter.clustersetting.web.tag.ClusterSettingTagsHandler.ClusterSettingTagsLinks;
import io.datarouter.storage.setting.DatarouterSettingTag;
import io.datarouter.storage.setting.DefaultSettingValue;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.web.html.j2html.J2HtmlTable;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.SpanTag;
import j2html.tags.specialized.TdTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

public record ClusterSettingBrowseSettingHtml(
		ClusterSettingBrowseLinks browseLinks,
		ClusterSettingLogLinks logLinks,
		ClusterSettingOverrideCreateLinks createOverrideLinks,
		ClusterSettingOverrideUpdateLinks updateOverrideLinks,
		ClusterSettingOverrideDeleteLinks deleteOverrideLinks,
		ClusterSettingTagsLinks tagsLinks,
		DatarouterClusterSettingDao settingDao,
		ClusterSettingService settingService,
		DatarouterClusterSettingLogDao logDao,
		ClusterSettingBrowseHandlerParams params){

	private static final String VALUE_STYLE = "background-color:#E5F8FF;word-break:break-all;";
	private static final String DEFAULT_VALUE_STYLE = "background-color:#E6E6E6;word-break:break-all;";

	public DivTag makeSettingDiv(CachedSetting<?> setting){
		String name = setting.getName();
		DefaultSettingValue<?> defaults = setting.getDefaultSettingValue();
		List<CodeOverrideRow> overrideRows = CodeOverridesTool.makeOverrideRows(setting);
		List<ClusterSetting> databaseSettings = settingDao.scanWithName(name).list();
		boolean isGlobalDefault = overrideRows.isEmpty() && databaseSettings.isEmpty();
		long logCount = logDao.countSettingLogs(name);
		var head = div();
		head.with(makeNameDiv(name).withClass("bg-light p-2"));
		var body = div()
				.withClass("p-2");
		body.with(makeValueDiv(setting, isGlobalDefault).withClass("my-1"));
		if(!isGlobalDefault){
			body.with(makeGlobalDefaultValueDiv(setting));
		}
		if(!overrideRows.isEmpty()){
			body.with(makeCodeOverridesDiv(overrideRows).withClass("my-3"));
		}
		if(defaults.hasTags()){
			body.with(makeTagOverridesDiv(setting).withClass("my-3"));
		}
		if(!databaseSettings.isEmpty()){
			body.with(makeDatabaseOverridesDiv(setting, databaseSettings).withClass("my-3"));
		}
		if(logCount > 0){
			body.with(makeSettingLogsDiv(name, logCount).withClass("my-2"));
		}
		return div(head, body)
				.withClass("border m-2");
	}

	private DivTag makeNameDiv(String name){
		var linkParams = new ClusterSettingBrowseHandlerParams()
				.withLocation(name)
				.withOptPartialName(params.partialName);
		String href = browseLinks.all(linkParams);
		var link = a(strong(shortName(name)))
				.withHref(href)
				.withStyle("font-size:1.1em;");
		var nameSpan = span(link);
		return div(nameSpan);
	}

	private DivTag makeValueDiv(
			CachedSetting<?> setting,
			boolean isDefault){
		var addOverrideLink = a("Override")
				.withHref(createOverrideLinks.create(
						Optional.of(ClusterSettingEditSource.CODE),
						params.partialName,
						Optional.of(setting.getName())));
		String stringValue = setting.toStringValue();
		boolean inline = stringValue.length() <= 20;//show small values inline
		String title = inline ? "Value:" : "Value";
		String subtext = isDefault ? "default" : "on this server";
		var valueSpan = span(setting.toStringValue())
				.withClass("ml-1 p-1")
				.withStyle(VALUE_STYLE);
		var valueLine = div();
		valueLine.with(strong(title));
		if(inline){
			valueLine.with(valueSpan);
		}
		valueLine.with(span(String.format("(%s)", subtext)).withClass("ml-2").withStyle("color:gray;"));
		valueLine.with(span("-").withClass("mx-2"));
		valueLine.with(addOverrideLink);
		var content = div(valueLine);
		if(!inline){//put bigger values below
			content.with(div(valueSpan).withClass("mt-2"));
		}
		return content;
	}

	private DivTag makeGlobalDefaultValueDiv(CachedSetting<?> setting){
		return div(
				span("Default: "),
				span(setting.toStringDefaultValue())
						.withClass("m-1 p-1")
						.withStyle(DEFAULT_VALUE_STYLE))
				.withClass("ml-1")
				.withStyle("font-size:.9em;");
	}

	private <T> DivTag makeCodeOverridesDiv(
			List<CodeOverrideRow> rows){
		var headerDiv = div(makeSectionHeaderSpan("Code Overrides", rows.size()));
		var tableBuilder = new J2HtmlTable<CodeOverrideRow>()
				.withClasses("table table-sm border");
		tableBuilder.withHtmlColumn("Winner", row -> makeActiveOrWinnerCell(row.winner()));
		tableBuilder.withHtmlColumn("Active", row -> makeActiveOrWinnerCell(row.active()));
		if(CodeOverrideRow.notEmpty(rows, CodeOverrideRow::environmentType)){
			tableBuilder.withColumn("Env Type", CodeOverrideRow::environmentType);
		}
		if(CodeOverrideRow.notEmpty(rows, CodeOverrideRow::environmentName)){
			tableBuilder.withColumn("Env Name", CodeOverrideRow::environmentName);
		}
		if(CodeOverrideRow.notEmpty(rows, CodeOverrideRow::environmentCategoryName)){
			tableBuilder.withColumn("Env Category", CodeOverrideRow::environmentCategoryName);
		}
		if(CodeOverrideRow.notEmpty(rows, CodeOverrideRow::serverType)){
			tableBuilder.withColumn("Server Type", CodeOverrideRow::serverType);
		}
		if(CodeOverrideRow.notEmpty(rows, CodeOverrideRow::serverName)){
			tableBuilder.withColumn("Server Name", CodeOverrideRow::serverName);
		}
		tableBuilder.withHtmlColumn("Value", row -> makeValueCell(row.value()));
		var tableDiv = div(tableBuilder.build(rows));
		var bodyDiv = div(
				tableDiv)
				.withStyle("font-size:.9em;");
		return div(
				headerDiv,
				bodyDiv);
	}

	private DivTag makeTagOverridesDiv(CachedSetting<?> setting){
		List<TagOverrideRow> rows = TagOverridesTool.makeRows(setting);
		var editTagsLink = a("Edit Tags")
				.withHref(tagsLinks.tags());
		var header = div(
				makeSectionHeaderSpan("Tag Overrides", rows.size()),
				span("-").withClass("mx-2"),
				editTagsLink);
		var table = new J2HtmlTable<TagOverrideRow>()
				.withClasses("table table-sm border")
				.withHtmlColumn("Active", row -> makeActiveOrWinnerCell(row.active()))
				.withColumn("Tag", TagOverrideRow::tag, DatarouterSettingTag::getPersistentString)
				.build(rows);
		var tableDiv = div(table)
				.withStyle("font-size:.9em;");
		return div(
				header,
				tableDiv);
	}

	private DivTag makeDatabaseOverridesDiv(
			CachedSetting<?> setting,
			List<ClusterSetting> dbSettings){
		Optional<ClusterSetting> mostSpecificSetting = settingService.getMostSpecificClusterSetting(dbSettings);
		List<DatabaseOverrideRow> rows = DatabaseOverridesTool.makeRows(setting, dbSettings, mostSpecificSetting);
		var addOverrideLink = a("Add")
				.withHref(createOverrideLinks.create(
						Optional.of(ClusterSettingEditSource.CODE),
						params.partialName,
						Optional.of(setting.getName())));
		var header = div(
				makeSectionHeaderSpan("Database Overrides", rows.size()),
				span("-").withClass("mx-2"),
				addOverrideLink);
		var tableBuilder = new J2HtmlTable<DatabaseOverrideRow>()
				.withClasses("table table-sm border");
		tableBuilder.withHtmlColumn("Winner", row -> makeActiveOrWinnerCell(row.winner()));
		tableBuilder.withHtmlColumn("Active", row -> makeActiveOrWinnerCell(row.active()));
		tableBuilder.withColumn("Scope", DatabaseOverrideRow::scope, scope -> scope.display);
		if(DatabaseOverrideRow.anyServerType(rows)){
			tableBuilder.withColumn("Server Type", DatabaseOverrideRow::serverType);
		}
		if(DatabaseOverrideRow.notEmpty(rows, DatabaseOverrideRow::serverName)){
			tableBuilder.withColumn("Server Name", DatabaseOverrideRow::serverName);
		}
		tableBuilder.withHtmlColumn("Value", row -> makeValueCell(row.value()));
		tableBuilder.withHtmlColumn("Edit", this::makeEditDatabaseOverrideCell);
		tableBuilder.withHtmlColumn("X", this::makeDeleteDatabaseOverrideCell);
		var tableDiv = div(tableBuilder.build(rows))
				.withStyle("font-size:.9em;");
		var div = div(header);
		if(!rows.isEmpty()){
			div.with(tableDiv);
		}
		return div;
	}

	private DivTag makeSettingLogsDiv(String settingName, long logCount){
		var logLink = a("View")
				.withHref(logLinks.setting(settingName));
		return div(
				makeSectionHeaderSpan("Logs", logCount),
				span("-").withClass("mx-2"),
				logLink);
	}

	/*---------- cells ------------*/

	private TdTag makeActiveOrWinnerCell(boolean active){
		String marginClass = "ml-2";
		var content = active
				? strong("Yes")
						.withClass(marginClass)
						.withStyle("color:limegreen;")
				: span("No")
						.withClass(marginClass)
						.withStyle("color:salmon;");
		return td(content)
				.withStyle("width:50px;");
	}

	private TdTag makeValueCell(String value){
		return td(value)
				.withStyle("width:250px;word-break:break-all;");
	}

	private TdTag makeEditDatabaseOverrideCell(DatabaseOverrideRow row){
		Optional<String> optServerType = ClusterSettingScope.SERVER_TYPE == row.scope()
				? Optional.of(row.serverType())
				: Optional.empty();
		Optional<String> optServerName = ClusterSettingScope.SERVER_NAME == row.scope()
				? Optional.of(row.serverName())
				: Optional.empty();
		String href = updateOverrideLinks.update(
				Optional.of(ClusterSettingEditSource.CODE),
				params.partialName,
				row.name(),
				optServerType,
				optServerName);
		var deleteOverrideLink = a("edit")
				.withHref(href);
		return td(deleteOverrideLink)
				.withStyle("width:50px;");
	}

	private TdTag makeDeleteDatabaseOverrideCell(DatabaseOverrideRow row){
		Optional<String> optServerType = ClusterSettingScope.SERVER_TYPE == row.scope()
				? Optional.of(row.serverType())
				: Optional.empty();
		Optional<String> optServerName = ClusterSettingScope.SERVER_NAME == row.scope()
				? Optional.of(row.serverName())
				: Optional.empty();
		String href = deleteOverrideLinks.delete(
				Optional.of(ClusterSettingEditSource.CODE),
				params.location,
				params.partialName,
				row.name(),
				optServerType,
				optServerName);
		var deleteOverrideLink = a("X")
				.withTitle("Delete")
				.withHref(href);
		return td(deleteOverrideLink)
				.withStyle("width:30px;");
	}

	/*--------- helper ------------*/

	private SpanTag makeSectionHeaderSpan(String name, long count){
		return span(
				strong(name),
				span(String.format("(%s)", count))
						.withStyle("color:gray;font-size:1em;"));
	}

	private String shortName(String settingName){
		int splitAt = settingName.lastIndexOf('.') + 1;
		return settingName.substring(splitAt, settingName.length());
	}

	@Singleton
	public static class ClusterSettingBrowseSettingHtmlFactory{
		@Inject
		private ClusterSettingBrowseLinks browseLinks;
		@Inject
		private ClusterSettingLogLinks logLinks;
		@Inject
		private ClusterSettingOverrideCreateLinks createOverrideLinks;
		@Inject
		private ClusterSettingOverrideUpdateLinks updateOverrideLinks;
		@Inject
		private ClusterSettingOverrideDeleteLinks deleteOverrideLinks;
		@Inject
		private ClusterSettingTagsLinks tagsLinks;
		@Inject
		private DatarouterClusterSettingDao settingDao;
		@Inject
		private ClusterSettingService settingService;
		@Inject
		private DatarouterClusterSettingLogDao logDao;

		public ClusterSettingBrowseSettingHtml create(ClusterSettingBrowseHandlerParams params){
			return new ClusterSettingBrowseSettingHtml(
					browseLinks,
					logLinks,
					createOverrideLinks,
					updateOverrideLinks,
					deleteOverrideLinks,
					tagsLinks,
					settingDao,
					settingService,
					logDao,
					params);
		}
	}

}