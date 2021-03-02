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
package io.datarouter.changelog.web;

import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h2;

import java.util.Optional;

import javax.inject.Inject;

import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.handler.types.optional.OptionalString;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.ContainerTag;

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

	@Handler(defaultHandler = true)
	public Mav insert(
			@Param(P_name) OptionalString name,
			@Param(P_action) OptionalString action,
			@Param(P_toEmail) OptionalString toEmail,
			@Param(P_note) OptionalString note,
			@Param(P_submitAction) OptionalString submitAction){
		var form = new HtmlForm()
				.withMethod("post");
		form.addTextField()
				.withDisplay("Name")
				.withName(P_name)
				.withPlaceholder("Xyz Migration")
				.withValue(name.orElse(null));
		form.addTextField()
				.withDisplay("Action")
				.withName(P_action)
				.withPlaceholder("Backfill")
				.withValue(action.orElse(null));
		form.addTextField()
				.withDisplay("Email To (csv) (Optional). All administrators are included by default")
				.withName(P_toEmail)
				.withPlaceholder("a@something.com,b@something.com")
				.withValue(toEmail.orElse(null));
		form.addTextAreaField()
				.withDisplay("Note (Optional)")
				.withName(P_note)
				.withPlaceholder("Migration for tables xyz")
				.withValue(note.orElse(null));
		form.addButton()
				.withDisplay("Record & Email")
				.withValue("anything");
		if(submitAction.isEmpty() || form.hasErrors()){
			return pageFactory.startBuilder(request)
					.withTitle("Manual Changelog")
					.withContent(Html.makeContent(form))
					.buildMav();
		}
		String username = getSessionInfo().getRequiredSession().getUsername();
		changelogRecorder.recordAndSendEmail(
				"ManualEntry",
				name.get(),
				action.get(),
				username,
				Optional.empty(),
				note.getOptional(),
				toEmail.getOptional());
		return pageFactory.preformattedMessage(request, "Recorded changelog entry.");
	}

	private static class Html{

		public static ContainerTag makeContent(HtmlForm htmlForm){
			var form = Bootstrap4FormHtml.render(htmlForm)
					.withClass("card card-body bg-light");
			return div(
					h2("Manual Changelog Recorder"),
					form,
					br())
					.withClass("container mt-3");
			}

	}

}
