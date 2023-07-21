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
import static j2html.TagCreator.h5;
import static j2html.TagCreator.li;
import static j2html.TagCreator.ul;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.auth.config.DatarouterAuthSettingRoot;
import io.datarouter.auth.storage.account.DatarouterAccount;
import io.datarouter.auth.storage.account.DatarouterAccountCredentialDao;
import io.datarouter.auth.storage.account.DatarouterAccountDao;
import io.datarouter.auth.storage.account.DatarouterAccountKey;
import io.datarouter.auth.storage.account.DatarouterAccountSecretCredentialDao;
import io.datarouter.auth.storage.accountpermission.DatarouterAccountPermission;
import io.datarouter.auth.storage.accountpermission.DatarouterAccountPermissionDao;
import io.datarouter.auth.storage.accountpermission.DatarouterAccountPermissionKey;
import io.datarouter.auth.storage.useraccountmap.DatarouterUserAccountMap;
import io.datarouter.auth.storage.useraccountmap.DatarouterUserAccountMapDao;
import io.datarouter.auth.storage.useraccountmap.DatarouterUserAccountMapKey;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDtoBuilder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.timer.PhaseTimer;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;

public class DatarouterAccountRenameHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterAccountRenameHandler.class);

	private static final String
			P_oldAccountName = "oldAccountName",
			P_newAccountName = "newAccountName",
			P_submitAction = "submitAction";

	@Inject
	private DatarouterAccountDao accountDao;
	@Inject
	private DatarouterAccountCredentialDao accountCredentialDao;
	@Inject
	private DatarouterAccountSecretCredentialDao secretCredentialDao;
	@Inject
	private DatarouterAccountPermissionDao accountPermissionDao;
	@Inject
	private DatarouterUserAccountMapDao userAccountMapDao;
	@Inject
	private ChangelogRecorder changelogRecorder;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterAuthSettingRoot authSettings;

	@Handler(defaultHandler = true)
	public Mav renameAccounts(
			@Param(P_oldAccountName) Optional<String> oldAccountName,
			@Param(P_newAccountName) Optional<String> newAccountName,
			@Param(P_submitAction) Optional<String> submitAction){
		String errorNewAccountName = null;
		if(submitAction.isPresent()){
			try{
				if(newAccountName.map(StringTool::nullIfEmpty).isPresent()){
					// here we can add new accountName validation and checks. (no spaces?)
				}
			}catch(Exception e){
				errorNewAccountName = "AccountName cannot have spaces";
			}
		}

		List<String> possibleOldAccountNames = accountDao.scanKeys()
				.map(DatarouterAccountKey::getAccountName)
				.sort()
				.list();

		var form = new HtmlForm()
				.withMethod("post");
		form.addSelectField()
				.withDisplay("Old AccountName")
				.withName(P_oldAccountName)
				.withValues(possibleOldAccountNames);
		form.addTextField()
				.withDisplay("New Account name")
				.withError(errorNewAccountName)
				.withName(P_newAccountName)
				.withPlaceholder("abc")
				.withValue(newAccountName.orElse(null));
		form.addButton()
				.withDisplay("Rename")
				.withValue("renameAccounts");

		if(submitAction.isEmpty() || form.hasErrors()){
			return pageFactory.startBuilder(request)
					.withTitle("Dataroutetr Account Rename")
					.withContent(makeContent(form))
					.buildMav();
		}

		if(possibleOldAccountNames.contains(newAccountName.get())){
			throw new RuntimeException(String.format("accountName=%s already exists for DatarouterAccount",
					newAccountName));
		}
		if(!possibleOldAccountNames.contains(oldAccountName.get())){
			throw new RuntimeException(String.format("accountName=%s does not exist for DatarouterAccount",
					oldAccountName));
		}
		rename(oldAccountName.get(), newAccountName.get());

		return pageFactory.message(request, "Account renamed from=" + oldAccountName.get()
				+ " to=" + newAccountName.get());
	}

	public DivTag makeContent(HtmlForm htmlForm){
		var form = Bootstrap4FormHtml.render(htmlForm)
				.withClass("card card-body bg-light");
		CachedSetting<DatarouterDuration> refreshSetting = authSettings.accountRefreshFrequencyDuration;

		return div(
				h2("Datarouter Account Rename"),
				form,
				br(),
				h5("Important Details"),
				ul(li("Requests using this account will be unavailable for a maximum of " + refreshSetting.get()
						+ ". You can mitigate this by temporarily decreasing the cluster setting "
						+ refreshSetting.getName()),
						li("Metrics utilizing the old account name will not be renamed")))
				.withClass("container mt-3");
	}

	private void rename(String oldAccountName, String newAccountName){
		PhaseTimer timer = new PhaseTimer("accountRename");

		assertNewAccountNameIsValid(newAccountName);
		timer.add("assertion");

		renameDatarouterAccount(oldAccountName, newAccountName);
		timer.add("datarouterAccount");

		renameDatarouterAccountCredential(oldAccountName, newAccountName);
		timer.add("accountCredential");

		renameDatarouterAccountSecretCredential(oldAccountName, newAccountName);
		timer.add("secretCredential");

		renameDatarouterAccountPermission(oldAccountName, newAccountName);
		timer.add("accountPermission");

		renameDatarouterUserAccountMaps(oldAccountName, newAccountName);
		timer.add("userAccountMaps");

		logger.warn("{}", timer.toString());
	}

	private void renameDatarouterAccount(String oldAccountName, String newAccountName){
		var oldAccount = accountDao.get(new DatarouterAccountKey(oldAccountName));
		var newAccount = new DatarouterAccount(newAccountName, oldAccount);
		accountDao.put(newAccount);
		accountDao.delete(oldAccount.getKey());
		recordChangelog(oldAccountName, newAccountName, "DatarouterAccount");
	}

	private void renameDatarouterAccountCredential(String oldAccountName, String newAccountName){
		accountCredentialDao.scanByAccountName(oldAccountName)
				.include(credential -> credential.getAccountName().equals(oldAccountName))
				.forEach(credential -> {
					credential.setAccountName(newAccountName);
					accountCredentialDao.put(credential);
					recordChangelog(oldAccountName, newAccountName, "DatarouterAccountCredential");
				});
	}

	private void renameDatarouterAccountSecretCredential(String oldAccountName, String newAccountName){
		secretCredentialDao.scan()
				.include(credential -> credential.getAccountName().equals(oldAccountName))
				.forEach(credential -> {
					credential.setAccountName(newAccountName);
					secretCredentialDao.updateIgnore(credential);
					recordChangelog(oldAccountName, newAccountName, "DatarouterAccountSecretCredential");
				});
	}

	private void renameDatarouterAccountPermission(String oldAccountName, String newAccountName){
		List<DatarouterAccountPermissionKey> oldKeys = accountPermissionDao.scanKeys()
				.include(key -> key.getAccountName().equals(oldAccountName))
				.list();
		if(oldKeys.isEmpty()){
			return;
		}
		List<DatarouterAccountPermission> newKeys = Scanner.of(oldKeys)
				.map(key -> new DatarouterAccountPermission(newAccountName, key.getEndpoint()))
				.list();
		accountPermissionDao.putMulti(newKeys);
		accountPermissionDao.deleteMulti(oldKeys);
		recordChangelog(oldAccountName, newAccountName, "DatarouterAccountPermission");
	}

	private void renameDatarouterUserAccountMaps(String oldAccountName, String newAccountName){
		List<DatarouterUserAccountMapKey> oldKeys = userAccountMapDao.scanKeys()
				.include(key -> key.getAccountName().equals(oldAccountName))
				.list();
		List<DatarouterUserAccountMap> newKeys = Scanner.of(oldKeys)
				.map(key -> new DatarouterUserAccountMap(key.getUserId(), newAccountName))
				.list();
		userAccountMapDao.putMulti(newKeys);
		userAccountMapDao.deleteMulti(oldKeys);
		recordChangelog(oldAccountName, newAccountName, "DatarouterAccountSecretCredential");
	}

	private void assertNewAccountNameIsValid(String newAccountName){
		if(accountDao.exists(new DatarouterAccountKey(newAccountName))){
			throw new RuntimeException(String.format("accountName=%s already exists for DatarouterAccount",
					newAccountName));
		}

		if(accountCredentialDao.exists(newAccountName)){
			throw new RuntimeException(String.format("accountName=%s already exists for DatarouterAccountCredential",
					newAccountName));
		}

		boolean containsSecretCredential = secretCredentialDao.scan()
				.anyMatch(credential -> credential.getAccountName().equals(newAccountName));
		if(containsSecretCredential){
			throw new RuntimeException(String.format(
					"accountName=%s already exists for DatarouterSecretAccountCredential", newAccountName));
		}

		boolean containsPermission = accountPermissionDao.scanKeys()
				.anyMatch(key -> key.getAccountName().equals(newAccountName));
		if(containsPermission){
			throw new RuntimeException(String.format(
					"accountName=%s already exists for DatarouterAccountPermission", newAccountName));
		}

		boolean containsUserAccountMappings = userAccountMapDao.scanKeys()
				.anyMatch(key -> key.getAccountName().equals(newAccountName));
		if(containsUserAccountMappings){
			throw new RuntimeException(String.format(
					"accountName=%s already exists for DatarouterUserAccountMap", newAccountName));
		}
	}

	private void recordChangelog(String oldAccountName, String newAccountName, String tableUpdated){
		var dto = new DatarouterChangelogDtoBuilder(
				DatarouterAccountManagerHandler.CHANGELOG_TYPE,
				oldAccountName + "->" + newAccountName,
				"rename " + tableUpdated,
				getSessionInfo().findNonEmptyUsername().orElse(""))
				.withComment(oldAccountName + "->" + newAccountName)
				.build();
		changelogRecorder.record(dto);
	}

}
