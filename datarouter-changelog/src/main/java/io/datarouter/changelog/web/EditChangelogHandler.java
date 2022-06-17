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
package io.datarouter.changelog.web;

import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h2;

import java.time.ZoneId;
import java.util.Optional;

import javax.inject.Inject;

import io.datarouter.changelog.storage.Changelog;
import io.datarouter.changelog.storage.ChangelogDao;
import io.datarouter.changelog.storage.ChangelogKey;
import io.datarouter.instrumentation.changelog.ChangelogDto;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.util.time.ZonedDateFormatterTool;
import io.datarouter.web.config.service.ServiceName;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.handler.types.optional.OptionalString;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.j2html.J2HtmlLegendTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.user.session.CurrentUserSessionInfoService;
import j2html.tags.specialized.DivTag;

public class EditChangelogHandler extends BaseHandler{

	public static final String P_reversedDateMs = "reversedDateMs";
	public static final String P_changelogType = "changelogType";
	public static final String P_name = "name";
	public static final String P_note = "note";
	private static final String P_submitAction = "submitAction";

	@Inject
	private ChangelogDao dao;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private CurrentUserSessionInfoService sessionInfoService;
	@Inject
	private ChangelogRecorder recorder;
	@Inject
	private ServiceName serviceName;

	@Handler(defaultHandler = true)
	public Mav edit(
			@Param(P_reversedDateMs) Long reversedDateMs,
			@Param(P_changelogType) String changelogType,
			@Param(P_name) String name,
			@Param(P_note) OptionalString note,
			@Param(P_submitAction) OptionalString submitAction){
		ChangelogKey key = new ChangelogKey(reversedDateMs, changelogType, name);
		Changelog changelog = dao.get(key);

		DivTag table = new J2HtmlLegendTable()
				.withClass("table table-sm border table-striped")
				.withSingleRow(false)
				.withEntry("Date", printDate(changelog))
				.withEntry("Changelog Type", changelog.getKey().getChangelogType())
				.withEntry("Name", changelog.getKey().getName())
				.withEntry("Action", changelog.getAction())
				.withEntry("Username", changelog.getUsername())
				.withEntry("Comment", Optional.ofNullable(changelog.getComment()).orElse(""))
				.withEntry("Note", Optional.ofNullable(changelog.getNote()).orElse(""))
				.build();

		var form = new HtmlForm()
				.withMethod("post");
		form.addTextAreaField()
				.withDisplay("Note (Optional)")
				.withName(P_note)
				.withPlaceholder(Optional.ofNullable(changelog.getNote()).orElse(""))
				.withValue(note.orElse(null));
		form.addButton()
				.withDisplay("update")
				.withValue("anything");
		if(submitAction.isEmpty() || form.hasErrors()){
			return pageFactory.startBuilder(request)
					.withTitle("Manual Changelog")
					.withContent(Html.makeContent(table, form))
					.buildMav();
		}
		note.ifPresent(newNote -> {
			changelog.setNote(newNote);
			ChangelogDto dto = changelog.toDto(serviceName.get());
			recorder.update(dto);
		});
		return pageFactory.preformattedMessage(request, "Updated changelog entry.");
	}

	private static class Html{

		public static DivTag makeContent(DivTag table, HtmlForm htmlForm){
			var form = Bootstrap4FormHtml.render(htmlForm)
					.withClass("card card-body bg-light");
			return div(
					h2("Edit Changelog"),
					table,
					form,
					br())
					.withClass("container mt-3");
			}

	}

	private String printDate(Changelog row){
		ZoneId zoneId = sessionInfoService.getZoneId(request);
		long reversedDateMs = row.getKey().getReversedDateMs();
		return ZonedDateFormatterTool.formatReversedLongMsWithZone(reversedDateMs, zoneId);
	}

}
