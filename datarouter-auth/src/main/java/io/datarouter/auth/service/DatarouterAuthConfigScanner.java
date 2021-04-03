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

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.auth.storage.account.BaseDatarouterAccountCredentialDao;
import io.datarouter.auth.storage.account.DatarouterAccountCredential;
import io.datarouter.auth.storage.user.DatarouterUserDao;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.autoconfig.ConfigScanDto;
import io.datarouter.web.autoconfig.ConfigScanResponseTool;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.databean.DatarouterUserKey;

@Singleton
public class DatarouterAuthConfigScanner{

	@Inject
	private BaseDatarouterAccountCredentialDao datarouterAccountCredentialDao;
	@Inject
	private DatarouterUserDao datarouterUserDao;
	@Inject
	private DefaultDatarouterAccountKeysSupplier defaultDatarouterAccountKeys;

	public ConfigScanDto checkDatarouterAccountsWithDefaultKeys(){
		List<String> accounts = datarouterAccountCredentialDao.scan()
				.include(credential -> StringTool.equalsCaseInsensitive(credential.getKey().getApiKey(),
						defaultDatarouterAccountKeys.getDefaultApiKey())
						|| StringTool.equalsCaseInsensitive(credential.getSecretKey(),
								defaultDatarouterAccountKeys.getDefaultSecretKey()))
				.map(DatarouterAccountCredential::getAccountName)
				.list();
		if(accounts.isEmpty()){
			return ConfigScanResponseTool.buildEmptyResponse();
		}
		String header = "Found " + accounts.size() + " account credential(s) with the default apiKey or secretKey";
		return ConfigScanResponseTool.buildResponse(header, accounts);
	}

	public ConfigScanDto checkForDefaultUserId(){
		Long defaultAdminId = DatarouterUserCreationService.ADMIN_ID;
		Optional<DatarouterUser> defaultUser = datarouterUserDao.find(new DatarouterUserKey(defaultAdminId));
		if(defaultUser.isEmpty()){
			return ConfigScanResponseTool.buildEmptyResponse();
		}
		String userName = defaultUser.get().getUsername();
		return ConfigScanResponseTool.buildResponse("Found a user with the default admin id=" + defaultAdminId
				+ " and username=" + userName);
	}

}
