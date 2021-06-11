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
package io.datarouter.web.email;

import static j2html.TagCreator.body;
import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.p;

import javax.inject.Inject;

import io.datarouter.email.email.DatarouterHtmlEmailService;
import io.datarouter.email.email.StandardDatarouterEmailHeaderService;
import io.datarouter.storage.config.DatarouterAdministratorEmailService;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.web.app.WebappName;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.handler.types.optional.OptionalString;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.ContainerTag;

public class EmailTestHandler extends BaseHandler{

	private static final String P_submitAction = "submitAction";

	@Inject
	private DatarouterHtmlEmailService htmlEmailService;
	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private WebappName webappName;
	@Inject
	private DatarouterWebPaths paths;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterAdministratorEmailService adminEmailService;
	@Inject
	private StandardDatarouterEmailHeaderService standardDatarouterEmailHeaderService;

	@Handler(defaultHandler = true)
	private Mav sendEmailTest(@Param(P_submitAction) OptionalString submitAction){
		var form = new HtmlForm();
		form.addButton()
				.withDisplay("Submit")
				.withValue("");
		if(submitAction.isEmpty()){
			return pageFactory.startBuilder(request)
					.withTitle("Send")
					.withContent(Html.makeContent(form))
					.buildMav();
		}
		String fromEmail = getSessionInfo().getRequiredSession().getUsername();
		String toEmail = adminEmailService.getAdministratorEmailAddressesCsv();
		String server = datarouterProperties.getServerName();
		String webapp = webappName.getName();
		String primaryHref = htmlEmailService.startLinkBuilder()
				.withLocalPath(paths.datarouter.emailTest)
				.build();
		var header = standardDatarouterEmailHeaderService.makeStandardHeader();
		String message = String.format("Test email sent to %s, from server %s, webapp %s. Email initiated by %s.",
				toEmail, server, webapp, fromEmail);
		var content = body(header, p(message));
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withTitle("Email Test")
				.withTitleHref(primaryHref)
				.withContent(content);
		htmlEmailService.trySendJ2Html(fromEmail, toEmail, emailBuilder);
		return pageFactory.message(request, message);
	}

	private static class Html{

		public static ContainerTag makeContent(HtmlForm htmlForm){
			var form = Bootstrap4FormHtml.render(htmlForm)
					.withClass("card card-body bg-light");
			return div(
					h2("Email Client Tester"),
					form,
					br())
					.withClass("container mt-3");
			}

	}

}
