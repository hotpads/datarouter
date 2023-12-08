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

import java.util.Optional;

import io.datarouter.changelog.config.DatarouterChangelogPaths;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDtoBuilder;
import io.datarouter.pathnode.PathNode;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlForm.HtmlFormMethod;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;

public class ManualChangelogHandler extends BaseHandler{

	private static final String P_name = "name";
	private static final String P_action = "action";
	private static final String P_toEmail = "toEmail";
	private static final String P_note = "note";
	private static final String P_submitAction = "submitAction";

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private ChangelogRecorder changelogRecorder;
	@Inject
	private DatarouterChangelogPaths paths;

	@Handler(defaultHandler = true)
	public Mav insert(
			@Param(P_name) Optional<String> name,
			@Param(P_action) Optional<String> action,
			@Param(P_toEmail) Optional<String> toEmail,
			@Param(P_note) Optional<String> note,
			@Param(P_submitAction) Optional<String> submitAction){
		var form = new HtmlForm(HtmlFormMethod.POST);
		form.addTextField()
				.withLabel("Name")
				.withName(P_name)
				.withPlaceholder("Xyz Migration")
				.withValue(name.orElse(null));
		form.addTextField()
				.withLabel("Action")
				.withName(P_action)
				.withPlaceholder("Backfill")
				.withValue(action.orElse(null));
		form.addTextField()
				.withLabel("Email To (csv) (Optional). All administrators are included by default")
				.withName(P_toEmail)
				.withPlaceholder("a@something.com,b@something.com")
				.withValue(toEmail.orElse(null));
		form.addTextAreaField()
				.withLabel("Note (Optional)")
				.withName(P_note)
				.withPlaceholder("Migration for tables xyz")
				.withValue(note.orElse(null));
		form.addButton()
				.withLabel("Record & Email")
				.withValue("anything");
		if(submitAction.isEmpty() || form.hasErrors()){
			return pageFactory.startBuilder(request)
					.withTitle("Manual Changelog")
					.withContent(Html.makeContent(paths.datarouter.changelog.insert, form))
					.buildMav();
		}
		String username = getSessionInfo().getRequiredSession().getUsername();
		DatarouterChangelogDtoBuilder dto = new DatarouterChangelogDtoBuilder("ManualEntry", name.get(), action.get(),
				username);
		note.ifPresent(dto::withNote);
		toEmail.ifPresent(email -> Scanner.of(email.split(",")).forEach(dto::additionalSendTos));
		dto.sendEmail();
		changelogRecorder.record(dto.build());
		return pageFactory.preformattedMessage(request, "Recorded changelog entry.");
	}

	private static class Html{

		public static DivTag makeContent(PathNode currentPath, HtmlForm htmlForm){
			var header = ChangelogHtml.makeHeader(currentPath);
			var form = Bootstrap4FormHtml.render(htmlForm)
					.withClass("card card-body bg-light");
			var formDiv = div(
					h4("Insert a Changelog"),
					form)
					.withClass("container mt-3");
			return div(
					header,
					formDiv,
					br())
					.withClass("container-fluid mt-3");
			}

	}

}
