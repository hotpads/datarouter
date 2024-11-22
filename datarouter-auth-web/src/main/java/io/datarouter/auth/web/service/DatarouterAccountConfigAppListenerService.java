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
package io.datarouter.auth.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.auth.service.DatarouterUserCreationService;
import io.datarouter.auth.storage.account.DatarouterAccount;
import io.datarouter.auth.storage.account.DatarouterAccountDao;
import io.datarouter.auth.storage.account.DatarouterAccountKey;
import io.datarouter.auth.storage.account.credential.DatarouterAccountCredential;
import io.datarouter.auth.storage.account.credential.DatarouterAccountCredentialDao;
import io.datarouter.auth.storage.account.credential.DatarouterAccountCredentialKey;
import io.datarouter.auth.storage.account.permission.DatarouterAccountPermission;
import io.datarouter.auth.storage.account.permission.DatarouterAccountPermissionDao;
import io.datarouter.auth.storage.account.permission.DatarouterAccountPermissionKey;
import io.datarouter.auth.storage.user.useraccountmap.BaseDatarouterUserAccountMapDao;
import io.datarouter.auth.storage.user.useraccountmap.DatarouterUserAccountMap;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.types.MilliTime;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterAccountConfigAppListenerService{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterAccountConfigAppListenerService.class);

	public static final String
			DEFAULT_ACCOUNT_NAME = "default";

	private static final long
			DEFAULT_ADMIN_ID = DatarouterUserCreationService.ADMIN_ID;
	private static final String
			DEFAULT_ACCOUNT_CREATOR = "defaultCreator",
			DEFAULT_ENDPOINT_ACCESS = DatarouterAccountPermissionKey.ALL_ENDPOINTS;

	@Inject
	private DatarouterAccountDao datarouterAccountDao;
	@Inject
	private DatarouterAccountCredentialDao datarouterAccountCredentialDao;
	@Inject
	private BaseDatarouterUserAccountMapDao userAccountMapDao;
	@Inject
	private DatarouterAccountPermissionDao accountPermissionDao;
	@Inject
	private DefaultDatarouterAccountKeysSupplier defaultDatarouterAccountKeys;
	@Inject
	private ServerTypeDetector serverTypeDetector;

	public void createDefaultAccountRecords(){
		if(serverTypeDetector.mightBeProduction()){
			return;
		}
		createDefaultAccountAndAdminUser();
		createDefaultAccountPermission();
	}

	private void createDefaultAccountAndAdminUser(){
		DatarouterAccount defaultAccount = datarouterAccountDao.get(new DatarouterAccountKey(DEFAULT_ACCOUNT_NAME));
		boolean accountExists = defaultAccount != null && defaultAccount.getEnableUserMappings();
		if(!accountExists){
			if(defaultAccount == null){
				defaultAccount = new DatarouterAccount(DEFAULT_ACCOUNT_NAME, MilliTime.now(), DEFAULT_ACCOUNT_CREATOR);
			}
			defaultAccount.setEnableUserMappings(true);
			datarouterAccountDao.put(defaultAccount);
			logger.warn("Created default DatarouterAccount");

			var userAccountMap = new DatarouterUserAccountMap(DEFAULT_ADMIN_ID, DEFAULT_ACCOUNT_NAME);
			userAccountMapDao.put(userAccountMap);
			logger.warn("Mapped the default admin user to the default account");
		}

		String defaultApiKey = defaultDatarouterAccountKeys.getDefaultApiKey();
		String defaultSecretKey = defaultDatarouterAccountKeys.getDefaultSecretKey();
		//TODO add active logic
		var accountCredsKey = new DatarouterAccountCredentialKey(defaultApiKey);
		var accountCreds = datarouterAccountCredentialDao.get(accountCredsKey);
		boolean keyExists = accountCreds != null && defaultSecretKey.equals(accountCreds.getSecretKey());
		if(!keyExists){
			accountCreds = new DatarouterAccountCredential(defaultApiKey, defaultSecretKey, DEFAULT_ACCOUNT_NAME,
					DEFAULT_ACCOUNT_CREATOR);
			datarouterAccountCredentialDao.put(accountCreds);
			logger.warn("Created default DatarouterAccountCredential");
		}
	}

	private void createDefaultAccountPermission(){
		var permission = new DatarouterAccountPermission(DEFAULT_ACCOUNT_NAME, DEFAULT_ENDPOINT_ACCESS);
		accountPermissionDao.put(permission);
		logger.warn("Created default DatarouterAccountPermission");
	}

}
