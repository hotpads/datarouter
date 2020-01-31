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

import io.datarouter.auth.storage.account.BaseDatarouterAccountDao;
import io.datarouter.auth.storage.account.DatarouterAccount;
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
public class DatarouterAccountCreationService{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterAccountCreationService.class);

	public static final String DEFAULT_ACCOUNT_NAME = "default";
	public static final String DEFAULT_ACCOUNT_CREATOR = "defaultCreator";

	@Inject
	private BaseDatarouterAccountDao datarouterAccountDao;
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
		if(datarouterAccountDao.exists(new DatarouterAccountKey(DEFAULT_ACCOUNT_NAME))){
			return;
		}
		DatarouterAccount account = new DatarouterAccount(DEFAULT_ACCOUNT_NAME, new Date(), DEFAULT_ACCOUNT_CREATOR);
		account.setEnableUserMappings(true);
		account.resetApiKeyToDefault(defaultDatarouterAccountKeys.getDefaultApiKey());
		account.resetSecretKeyToDefault(defaultDatarouterAccountKeys.getDefaultSecretKey());
		datarouterAccountDao.put(account);
		logger.warn("Created default DatarouterAccount");

		userAccountMapDao.put(new DatarouterUserAccountMap(DatarouterUserCreationService.ADMIN_ID,
				DEFAULT_ACCOUNT_NAME));
		logger.warn("Mapped the default admin user to the default account");
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
