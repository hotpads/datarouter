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
import static j2html.TagCreator.h4;

import java.time.ZoneId;
import java.util.Optional;

import io.datarouter.auth.service.CurrentUserSessionInfoService;
import io.datarouter.changelog.config.DatarouterChangelogPaths;
import io.datarouter.changelog.service.ViewChangelogService;
import io.datarouter.changelog.storage.Changelog;
import io.datarouter.changelog.storage.ChangelogDao;
import io.datarouter.changelog.storage.ChangelogKey;
import io.datarouter.instrumentation.changelog.ChangelogDto;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.pathnode.PathNode;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.types.MilliTimeReversed;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.InContextRedirectMav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlForm.HtmlFormMethod;
import io.datarouter.web.html.j2html.J2HtmlLegendTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;

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
	@Inject
	private DatarouterChangelogPaths paths;
	@Inject
	private ViewChangelogService viewChangelogService;

	@Handler(defaultHandler = true)
	public Mav edit(
			@Param(P_reversedDateMs)
			MilliTimeReversed reversedDateMs,
			@Param(P_changelogType)
			String changelogType,
			@Param(P_name)
			String name,
			@Param(P_note)
			Optional<String> note,
			@Param(P_submitAction)
			Optional<String> submitAction){
		var key = new ChangelogKey(reversedDateMs, changelogType, name);
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

		var form = new HtmlForm(HtmlFormMethod.POST);
		form.addTextAreaField()
				.withLabel("Note (Optional)")
				.withName(P_note)
				.withPlaceholder(Optional.ofNullable(changelog.getNote()).orElse(""))
				.withValue(note.orElse(null));
		form.addButton()
				.withLabel("update")
				.withValue("anything");
		if(submitAction.isEmpty() || form.hasErrors()){
			return pageFactory.startBuilder(request)
					.withTitle("Manual Changelog")
					.withContent(Html.makeContent(paths.datarouter.changelog.edit, table, form))
					.buildMav();
		}
		note.ifPresent(newNote -> {
			changelog.setNote(newNote);
			ChangelogDto dto = changelog.toDto(serviceName.get());
			recorder.update(dto);
		});
		String href = viewChangelogService.buildViewExactHref(changelog, false);
		return new InContextRedirectMav(request, href);
	}

	private static class Html{

		public static DivTag makeContent(PathNode currentPath, DivTag table, HtmlForm htmlForm){
			var header = ChangelogHtml.makeHeader(currentPath);
			var form = Bootstrap4FormHtml.render(htmlForm)
					.withClass("card card-body bg-light");
			var container = div(
					h4("Edit Changelog Note"),
					table,
					form)
					.withClass("container");
			return div(
					header,
					br(),
					container,
					br())
					.withClass("container-fluid");
			}

	}

	private String printDate(Changelog row){
		ZoneId zoneId = sessionInfoService.getZoneId(request);
		return row.getKey().getMilliTimeReversed().format(zoneId);
	}

}
