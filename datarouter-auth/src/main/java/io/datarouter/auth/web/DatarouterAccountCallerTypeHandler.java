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

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import io.datarouter.auth.service.AccountCallerTypeRegistry2;
import io.datarouter.auth.storage.account.DatarouterAccount;
import io.datarouter.auth.storage.account.DatarouterAccountDao;
import io.datarouter.auth.storage.account.DatarouterAccountKey;
import io.datarouter.httpclient.endpoint.caller.CallerType;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDtoBuilder;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.specialized.DivTag;

public class DatarouterAccountCallerTypeHandler extends BaseHandler{

	private static final String
			P_accountName = "accountName",
			P_callerType = "callerType",
			P_submitAction = "submitAction";

	@Inject
	private DatarouterAccountDao accountDao;
	@Inject
	private ChangelogRecorder changelogRecorder;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private AccountCallerTypeRegistry2 accountCallerTypeRegistry;

	@Handler(defaultHandler = true)
	public Mav updateCallerType(
			@Param(P_accountName) Optional<String> accountName,
			@Param(P_callerType) Optional<String> callerType,
			@Param(P_submitAction) Optional<String> submitAction){
		List<String> possibleOldAccountNames = accountDao.scanKeys()
				.map(DatarouterAccountKey::getAccountName)
				.sort()
				.list();
		List<String> possibleCallerTypes = accountCallerTypeRegistry.get().stream()
				.map(ReflectionTool::create)
				.map(CallerType::getName)
				.sorted()
				.distinct()
				.toList();

		var form = new HtmlForm()
				.withMethod("post");
		form.addSelectField()
				.withDisplay("Account Name")
				.withName(P_accountName)
				.withValues(possibleOldAccountNames);
		form.addSelectField()
				.withDisplay("Caller Type")
				.withName(P_callerType)
				.withValues(possibleCallerTypes);
		form.addButton()
				.withDisplay("Update")
				.withValue("anything");

		if(submitAction.isEmpty() || form.hasErrors()){
			return pageFactory.startBuilder(request)
					.withTitle("Datarouter Account Update Caller Type")
					.withContent(Html.makeContent(form))
					.buildMav();
		}

		updateCallerType(accountName.get(), callerType.get());

		return pageFactory.message(request, "Account=" + accountName.get() + " updated callerType to="
		+ callerType.get());
	}

	private static class Html{

		public static DivTag makeContent(HtmlForm htmlForm){
			var form = Bootstrap4FormHtml.render(htmlForm)
					.withClass("card card-body bg-light");
			return div(
					h2("Datarouter Account Caller Type Updater"),
					form,
					br())
					.withClass("container mt-3");
		}

	}

	private void updateCallerType(String accountName, String callerType){
		DatarouterAccount account = accountDao.get(new DatarouterAccountKey(accountName));
		String perviousCallerType = Optional.ofNullable(account.getCallerType())
				.orElse("");
		account.setCallerType(callerType);
		accountDao.put(account);

		var dto = new DatarouterChangelogDtoBuilder(
				DatarouterAccountManagerHandler.CHANGELOG_TYPE,
				accountName,
				"callerType",
				getSessionInfo().findNonEmptyUsername().orElse(""))
				.withComment(perviousCallerType + "->" + callerType)
				.build();
		changelogRecorder.record(dto);
	}

}
