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
package io.datarouter.auth.web.web;

import static j2html.TagCreator.div;
import static j2html.TagCreator.h2;

import java.util.List;

import io.datarouter.auth.config.DatarouterAuthenticationConfig;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlForm.HtmlFormMethod;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import j2html.tags.specialized.DivTag;

public class CreateUserFormHtml{

	private final List<String> roles;
	private final DatarouterAuthenticationConfig authenticationConfig;
	private final String submitAction;

	public CreateUserFormHtml(
			List<String> roles,
			DatarouterAuthenticationConfig authenticationConfig,
			String submitAction){
		this.roles = roles;
		this.authenticationConfig = authenticationConfig;
		this.submitAction = submitAction;
	}

	public DivTag build(){
		return div(
				h2("Create User"),
				Bootstrap4FormHtml.render(makeForm()))
				.withClass("container mt-3");
	}

	private HtmlForm makeForm(){
		var form = new HtmlForm(HtmlFormMethod.POST);
		form.addEmailField()
				.withName(authenticationConfig.getUsernameParam())
				.withLabel("Email")
				.withPlaceholder("you@email.com")
				.required();
		form.addPasswordField()
				.withName(authenticationConfig.getPasswordParam())
				.withLabel("Password")
				.required();
		form.addSelectField()
				.withName(authenticationConfig.getUserRolesParam())
				.withLabel("Roles")
				.withValues(roles)
				.multiple();
		form.addButton()
				.withLabel("Submit")
				.withValue(submitAction);
		return form;
	}

}
