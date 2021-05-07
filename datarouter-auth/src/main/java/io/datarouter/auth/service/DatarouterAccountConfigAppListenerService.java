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
package io.datarouter.auth.service;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.auth.storage.account.BaseDatarouterAccountCredentialDao;
import io.datarouter.auth.storage.account.BaseDatarouterAccountDao;
import io.datarouter.auth.storage.account.DatarouterAccount;
import io.datarouter.auth.storage.account.DatarouterAccountCredential;
import io.datarouter.auth.storage.account.DatarouterAccountCredentialKey;
import io.datarouter.auth.storage.account.DatarouterAccountKey;
import io.datarouter.auth.storage.accountpermission.BaseDatarouterAccountPermissionDao;
import io.datarouter.auth.storage.accountpermission.DatarouterAccountPermission;
import io.datarouter.auth.storage.accountpermission.DatarouterAccountPermissionKey;
import io.datarouter.auth.storage.useraccountmap.BaseDatarouterUserAccountMapDao;
import io.datarouter.auth.storage.useraccountmap.DatarouterUserAccountMap;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.util.lang.ObjectTool;

@Singleton
public class DatarouterAccountConfigAppListenerService{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterAccountConfigAppListenerService.class);

	public static final String DEFAULT_ACCOUNT_NAME = "default";
	public static final String DEFAULT_ACCOUNT_CREATOR = "defaultCreator";

	@Inject
	private BaseDatarouterAccountDao datarouterAccountDao;
	@Inject
	private BaseDatarouterAccountCredentialDao datarouterAccountCredentialDao;
	@Inject
	private BaseDatarouterUserAccountMapDao userAccountMapDao;
	@Inject
	private BaseDatarouterAccountPermissionDao accountPermissionDao;
	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private DefaultDatarouterAccountKeysSupplier defaultDatarouterAccountKeys;

	public void createDefaultAccountAndMapToDefaultAdminUser(){
		if(ObjectTool.notEquals(datarouterProperties.getServerTypeString(), ServerType.DEV.getPersistentString())){
			return;
		}

		DatarouterAccount defaultAccount = datarouterAccountDao.get(new DatarouterAccountKey(DEFAULT_ACCOUNT_NAME));
		boolean accountExists = defaultAccount != null && defaultAccount.getEnableUserMappings();
		if(!accountExists){
			if(defaultAccount == null){
				defaultAccount = new DatarouterAccount(DEFAULT_ACCOUNT_NAME, new Date(),DEFAULT_ACCOUNT_CREATOR);
			}
			defaultAccount.setEnableUserMappings(true);
			datarouterAccountDao.put(defaultAccount);
			logger.warn("Created default DatarouterAccount");

			userAccountMapDao.put(new DatarouterUserAccountMap(DatarouterUserCreationService.ADMIN_ID,
					DEFAULT_ACCOUNT_NAME));
			logger.warn("Mapped the default admin user to the default account");
		}

		String defaultApiKey = defaultDatarouterAccountKeys.getDefaultApiKey();
		String defaultSecretKey = defaultDatarouterAccountKeys.getDefaultSecretKey();
		//TODO add active logic
		DatarouterAccountCredential defaultCredential = datarouterAccountCredentialDao.get(
				new DatarouterAccountCredentialKey(defaultApiKey));
		boolean keyExists = defaultCredential != null && defaultSecretKey.equals(defaultCredential.getSecretKey());
		if(!keyExists){
			defaultCredential = new DatarouterAccountCredential(defaultApiKey, defaultSecretKey, DEFAULT_ACCOUNT_NAME,
					DEFAULT_ACCOUNT_CREATOR);
			datarouterAccountCredentialDao.put(defaultCredential);
			logger.warn("Created default DatarouterAccountCredential");
		}
	}

	public void createDefaultAccountPermission(){
		if(ObjectTool.notEquals(datarouterProperties.getServerTypeString(), ServerType.DEV.getPersistentString())){
			return;
		}
		accountPermissionDao.put(new DatarouterAccountPermission(DEFAULT_ACCOUNT_NAME,
				DatarouterAccountPermissionKey.ALL_ENDPOINTS));
		logger.warn("Created default DatarouterAccountPermission");
	}

}
