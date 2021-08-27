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
package io.datarouter.auth.web;

import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h2;

import javax.inject.Inject;

import io.datarouter.auth.config.DatarouterAuthPaths;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;
import io.datarouter.web.user.authenticate.saml.DatarouterSamlSettings;
import io.datarouter.web.user.authenticate.saml.SamlService;
import j2html.tags.ContainerTag;

public class DatarouterSigninHandler extends BaseHandler{

	@Inject
	private DatarouterAuthenticationConfig authenticationConfig;
	@Inject
	private DatarouterSamlSettings samlSettings;
	@Inject
	private SamlService samlService;
	@Inject
	private DatarouterAuthPaths paths;
	@Inject
	private Bootstrap4PageFactory pageFactory;

	@Handler(defaultHandler = true)
	private Mav showForm(){
		if(samlSettings.getShouldProcess()){
			samlService.redirectToIdentityProvider(request, response);
			return null;
		}
		var form = new HtmlForm()
				.withAction(request.getContextPath() + paths.signin.submit.toSlashedString())
				.withMethod("post");
		form.addEmailField()
				.autofocus()
				.withDisplay("Username")
				.withName(authenticationConfig.getUsernameParam());
		form.addPasswordField()
				.withDisplay("Password")
				.withName(authenticationConfig.getPasswordParam());
		form.addButton()
				.withDisplay("Submit")
				.withValue("anything");
		return pageFactory.startBuilder(request)
				.withTitle("Sign in")
				.includeNav(false)
				.withContent(Html.makeContent(form))
				.buildMav();
	}

	private static class Html{

		public static ContainerTag<?> makeContent(HtmlForm htmlForm){
			var form = Bootstrap4FormHtml.render(htmlForm)
					.withClass("card card-body bg-light");
			return div(
					h2("Sign in"),
					form,
					br())
					.withClass("container mt-3");
		}

	}

}
