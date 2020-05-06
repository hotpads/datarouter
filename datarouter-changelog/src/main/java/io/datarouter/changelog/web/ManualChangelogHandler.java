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
import static j2html.TagCreator.text;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import io.datarouter.changelog.config.DatarouterChangelogPaths;
import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.storage.config.DatarouterAdministratorEmailService;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.util.tuple.Twin;
import io.datarouter.web.email.DatarouterHtmlEmailService;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.handler.types.optional.OptionalString;
import io.datarouter.web.html.email.J2HtmlEmailTable;
import io.datarouter.web.html.email.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;

public class ManualChangelogHandler extends BaseHandler{

	private static final String P_name = "name";
	private static final String P_action = "action";
	private static final String P_toEmail = "toEmail";
	private static final String P_comment = "comment";
	private static final String P_submitAction = "submitAction";

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private ChangelogRecorder changelogRecorder;
	@Inject
	private DatarouterService datarouterService;
	@Inject
	private DatarouterHtmlEmailService htmlEmailService;
	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private DatarouterAdministratorEmailService additionalAdministratorEmailService;
	@Inject
	private DatarouterChangelogPaths paths;

	@Handler(defaultHandler = true)
	private Mav defaultHandler(
			@Param(P_name) OptionalString name,
			@Param(P_action) OptionalString action,
			@Param(P_toEmail) OptionalString toEmail,
			@Param(P_comment) OptionalString comment,
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
				.withDisplay("Email To (csv)")
				.withName(P_toEmail)
				.withPlaceholder("a@something.com,b@something.com")
				.withValue(toEmail.orElse(null));
		form.addTextField()
				.withDisplay("Comment (Optional)")
				.withName(P_comment)
				.withPlaceholder("Migration for tables xyz")
				.withValue(toEmail.orElse(null));
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
		changelogRecorder.record(
				"ManualEntry",
				name.get(),
				action.get(),
				username,
				getSessionInfo().getRequiredSession().getUserToken(),
				comment.orElse(null));
		sendEmail(name.get(), action.get(), username, toEmail.getOptional(), comment.getOptional());
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


	private void sendEmail(String name, String action, String username, Optional<String> toEmail,
			Optional<String> comment){
		String from = datarouterProperties.getAdministratorEmail();
		String to = toEmail
				.map(email -> email.split(","))
				.map(Set::of)
				.map(emailSet -> additionalAdministratorEmailService.getAdministratorEmailAddressesCsv(emailSet))
				.orElseGet(() -> additionalAdministratorEmailService.getAdministratorEmailAddressesCsv());

		String primaryHref = htmlEmailService.startLinkBuilder()
				.withLocalPath(paths.datarouter.changelog.view)
				.build();
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withSubject("Manual Changelog Entry - " + datarouterService.getName())
				.withTitle("Manual Changelog Entry")
				.withTitleHref(primaryHref)
				.withContent(makeEmailContent(name, action, username, comment.orElse("")));
		htmlEmailService.trySendJ2Html(from, to, emailBuilder);
	}

	private ContainerTag makeEmailContent(String name, String action, String username, String comment){
		var rows = List.of(
				new Twin<>("Service", datarouterService.getName()),
				new Twin<>("ChangelogType", "ManualEntry"),
				new Twin<>("Name", name),
				new Twin<>("Action", action),
				new Twin<>("Username", username),
				new Twin<>("Comment", comment));
		return new J2HtmlEmailTable<Twin<String>>()
				.withColumn(new J2HtmlEmailTableColumn<>(null, row -> makeDivBoldRight(row.getLeft())))
				.withColumn(new J2HtmlEmailTableColumn<>(null, row -> text(row.getRight())))
				.build(rows);
	}

	private static DomContent makeDivBoldRight(String text){
		return div(text).withStyle("font-weight:bold;text-align:right;");
	}

}
