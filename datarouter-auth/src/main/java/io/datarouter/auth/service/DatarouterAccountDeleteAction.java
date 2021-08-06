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
package io.datarouter.auth.service;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.auth.config.DatarouterAuthExecutors.DatarouterAccountDeleteActionExecutor;
import io.datarouter.auth.storage.account.DatarouterAccount;
import io.datarouter.auth.storage.useraccountmap.DatarouterUserAccountMapDao;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDtoBuilder;
import io.datarouter.storage.config.DatarouterProperties;

@Singleton
public class DatarouterAccountDeleteAction{

	@Inject
	private DatarouterUserAccountMapDao userAccountMapDao;
	@Inject
	private ChangelogRecorder changelogRecorder;
	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private DatarouterAccountDeleteActionExecutor executor;

	// DatarouterAccount will be deleted from the DB after this method is called
	public final void onDelete(DatarouterAccount account){
		executor.submit(() -> {
			additionalOnDeleteActions(account);
			cleanupDatarouterUserAccounts(account);
		});
	}

	private void cleanupDatarouterUserAccounts(DatarouterAccount account){
		if(!account.getEnableUserMappings()){
			return;
		}
		userAccountMapDao.scanKeys()
				.include(key -> key.getAccountName().equals(account.getKey().getAccountName()))
				.flush(userAccountMapDao::deleteMulti);
		recordChangelog(account, "cleanup datarouter users");
	}

	/*
	 * Override this method in sub classes and bind in plugin for additional cleanup actions
	 */
	protected void additionalOnDeleteActions(@SuppressWarnings("unused") DatarouterAccount account){
	}

	protected final void recordChangelog(DatarouterAccount account, String action){
		var changelogBuilder = new DatarouterChangelogDtoBuilder(
				"DatarouterAccount",
				account.getKey().getAccountName(),
				action,
				datarouterProperties.getAdministratorEmail());
		changelogRecorder.record(changelogBuilder.build());
	}

}
