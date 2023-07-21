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
package io.datarouter.clustersetting.web.log;

import static j2html.TagCreator.a;
import static j2html.TagCreator.b;
import static j2html.TagCreator.dd;
import static j2html.TagCreator.div;
import static j2html.TagCreator.dl;
import static j2html.TagCreator.dt;
import static j2html.TagCreator.span;
import static j2html.TagCreator.table;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;
import static j2html.TagCreator.tr;

import java.time.ZoneId;

import io.datarouter.clustersetting.enums.ClusterSettingLogAction;
import io.datarouter.clustersetting.enums.ClusterSettingScope;
import io.datarouter.clustersetting.storage.clustersettinglog.ClusterSettingLog;
import io.datarouter.clustersetting.storage.clustersettinglog.ClusterSettingLogKey;
import io.datarouter.clustersetting.web.ClusterSettingHtml;
import io.datarouter.clustersetting.web.browse.ClusterSettingBrowseHandler.ClusterSettingBrowseHandlerParams;
import io.datarouter.clustersetting.web.browse.ClusterSettingBrowseHandler.ClusterSettingBrowseLinks;
import io.datarouter.clustersetting.web.log.ClusterSettingLogHandler.ClusterSettingLogLinks;
import io.datarouter.util.time.ZonedDateFormatterTool;
import io.datarouter.web.html.j2html.J2HtmlTable;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TdTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ClusterSettingLogHtml{

	@Inject
	private ClusterSettingBrowseLinks browseLinks;
	@Inject
	private ClusterSettingLogLinks clusterSettingLogLinks;
	@Inject
	private ClusterSettingHtml clusterSettingHtml;

	/*-------- card ----------*/

	public DivTag makeCard(ZoneId zoneId, ClusterSettingLog log){
		String time = ZonedDateFormatterTool.formatInstantWithZoneDesc(
				log.getKey().getCreatedInstant(),
				zoneId);
		String browseHref = browseLinks.all(new ClusterSettingBrowseHandlerParams()
				.withLocation(log.getKey().getName()));
		var cardTitle = div(b(time))
				.withClass("bg-light p-3");
		var nameField = dl(
				dt("Setting Name"),
				dd(a(log.getKey().getName()).withHref(browseHref)));
		var smallFields = table()
				.withClass("mt-2");
		smallFields.with(tr(
				th("User").withStyle("width:250px"),
				th("Action").withStyle("width:150px"),
				th("Scope").withStyle("width:150px"),
				th("Server Type").withStyle("width:150px"),
				th("Server Name").withStyle("width:200px")));
		smallFields.with(tr(
				td(log.getChangedBy()),
				td(span(log.getAction().persistentString).withStyle(makeActionStyle(log.getAction()))),
				td(log.getScope().persistentString),
				td(log.getServerType()),
				td(log.getServerName())));
		var bigFields = dl(
				dt("Value"),
				dd(log.getValue()),
				dt("Comment"),
				dd(log.getComment()))
				.withClass("mt-2");
		var cardBody = div(
				nameField,
				smallFields,
				bigFields)
				.withClass("card-body");
		return div(
				cardTitle,
				cardBody)
				.withClass("card");
	}

	private static TdTag makeActionCell(ClusterSettingLogAction action){
		String cssClass = switch(action){
			case INSERTED -> "table-success";
			case UPDATED -> "table-info";
			case DELETED -> "table-danger";
		};
		return td(action.persistentString)
				.withClass(cssClass);
	}

	/*--------- table ----------*/

	public J2HtmlTable<ClusterSettingLog> makeTableBuilder(ZoneId zoneId, boolean showServerName){
		var tableBuilder = new J2HtmlTable<ClusterSettingLog>()
				.withClasses("sortable table table-sm table-striped border")
				.withHtmlColumn(
						"Time",
						row -> makeTimeCell(row.getKey(), zoneId))
				.withHtmlColumn(
						"Name",
						this::makeNameCell)
				.withHtmlColumn(
						"Action",
						row -> ClusterSettingLogHtml.makeActionCell(row.getAction()))
				.withColumn(
						"Scope",
						ClusterSettingLog::getScope,
						ClusterSettingScope.BY_PERSISTENT_STRING::toKey)
				.withColumn(
						"Server Type",
						ClusterSettingLog::getServerType);
		if(showServerName){
			tableBuilder
					.withColumn(
							"Server Name",
							ClusterSettingLog::getServerName);
		}
		return tableBuilder
				.withColumn(
						"Changed By",
						ClusterSettingLog::getChangedBy)
				.withHtmlColumn(
						"Comment",
						this::makeCommentCell)
				.withHtmlColumn(
						"Value",
						this::makeValueCell);
	}

	private TdTag makeTimeCell(ClusterSettingLogKey key, ZoneId zoneId){
		String text = ZonedDateFormatterTool.formatInstantWithZoneDesc(key.getCreatedInstant(), zoneId);
		String href = clusterSettingLogLinks.single(key.getName(), key.getReverseCreatedMs());
		var link = a(text).withHref(href);
		return td(link);
	}

	private TdTag makeNameCell(ClusterSettingLog log){
		String browseHref = browseLinks.all(new ClusterSettingBrowseHandlerParams()
				.withLocation(log.getKey().getName()));
		var link = a(log.getKey().getName()).withHref(browseHref);
		return td(link);
	}

	private TdTag makeCommentCell(ClusterSettingLog log){
		String detailsHref = clusterSettingLogLinks.single(log.getKey().getName(), log.getKey().getReverseCreatedMs());
		return clusterSettingHtml.makeLimitedLengthLinkCell(log.getComment(), detailsHref);
	}

	private TdTag makeValueCell(ClusterSettingLog log){
		String detailsHref = clusterSettingLogLinks.single(log.getKey().getName(), log.getKey().getReverseCreatedMs());
		return clusterSettingHtml.makeLimitedLengthLinkCell(log.getValue(), detailsHref);
	}

	private static String makeActionStyle(ClusterSettingLogAction action){
		return "color:" + switch(action){
			case INSERTED -> "green";
			case UPDATED -> "gray";
			case DELETED -> "red";
		};
	}

}