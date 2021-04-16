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

import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import io.datarouter.auth.cache.DatarouterAccountPermissionKeysByPrefixCache;
import io.datarouter.auth.config.DatarouterAuthExecutors.DatarouterAccountCredentialCacheExecutor;
import io.datarouter.auth.storage.account.BaseDatarouterAccountCredentialDao;
import io.datarouter.auth.storage.account.BaseDatarouterAccountSecretCredentialDao;
import io.datarouter.auth.storage.account.DatarouterAccountCredential;
import io.datarouter.auth.storage.account.DatarouterAccountCredentialKey;
import io.datarouter.auth.storage.account.DatarouterAccountSecretCredential;
import io.datarouter.auth.storage.account.DatarouterAccountSecretCredentialKey;
import io.datarouter.auth.storage.accountpermission.DatarouterAccountPermissionKey;
import io.datarouter.auth.web.DatarouterAccountManagerHandler.AccountCredentialDto;
import io.datarouter.httpclient.security.SecurityParameters;
import io.datarouter.scanner.Scanner;
import io.datarouter.secret.op.SecretOpReason;
import io.datarouter.secret.service.SecretNamespacer;
import io.datarouter.secret.service.SecretService;
import io.datarouter.secretweb.service.WebSecretOpReason;
import io.datarouter.util.Require;
import io.datarouter.web.user.session.service.Session;
import io.datarouter.web.util.PasswordTool;
import io.datarouter.web.util.http.RequestTool;

@Singleton
public class DatarouterAccountCredentialService{

	private static final String SECRET_NAMESPACE_SUFFIX = "drSecretCredentials/";

	private final BaseDatarouterAccountCredentialDao datarouterAccountCredentialDao;
	private final BaseDatarouterAccountSecretCredentialDao datarouterAccountSecretCredentialDao;
	private final DatarouterAccountPermissionKeysByPrefixCache datarouterAccountPermissionKeysByPrefixCache;
	private final DatarouterAccountLastUsedDateService datarouterAccountLastUsedDateService;
	private final SecretService secretService;
	private final SecretNamespacer secretNamespacer;

	private final AtomicReference<Map<String,AccountKey>> credentialAccountKeyByApiKey;
	private final AtomicReference<Map<String,String>> secretCredentialApiKeyBySecretName;
	private final AtomicReference<Map<String,AccountKey>> secretCredentialAccountKeyByApiKey;

	@Inject
	public DatarouterAccountCredentialService(
			BaseDatarouterAccountCredentialDao datarouterAccountCredentialDao,
			BaseDatarouterAccountSecretCredentialDao datarouterAccountSecretCredentialDao,
			DatarouterAccountPermissionKeysByPrefixCache datarouterAccountPermissionKeysByPrefixCache,
			DatarouterAccountLastUsedDateService datarouterAccountLastUsedDateService,
			DatarouterAccountCredentialCacheExecutor executor,
			SecretService secretService,
			SecretNamespacer secretNamespacer){
		this.datarouterAccountCredentialDao = datarouterAccountCredentialDao;
		this.datarouterAccountSecretCredentialDao = datarouterAccountSecretCredentialDao;
		this.datarouterAccountPermissionKeysByPrefixCache = datarouterAccountPermissionKeysByPrefixCache;
		this.datarouterAccountLastUsedDateService = datarouterAccountLastUsedDateService;
		this.secretService = secretService;
		this.secretNamespacer = secretNamespacer;

		credentialAccountKeyByApiKey = new AtomicReference<>(new HashMap<>());
		secretCredentialApiKeyBySecretName = new AtomicReference<>(new HashMap<>());
		secretCredentialAccountKeyByApiKey = new AtomicReference<>(new HashMap<>());
		refreshCaches();
		executor.scheduleWithFixedDelay(this::refreshCaches, 30, 30, TimeUnit.SECONDS);
	}

	//intended for API key auth (updates last used date of key)
	public Scanner<DatarouterAccountPermissionKey> scanPermissionsForApiKeyAuth(String apiKey){
		return findAccountKeyApiKeyAuth(apiKey, true)
				.map(accountKey -> accountKey.accountName)
				.map(DatarouterAccountPermissionKey::new)
				.map(datarouterAccountPermissionKeysByPrefixCache::get)
				.map(Scanner::of)
				.orElseGet(Scanner::empty);
	}

	//intended for API key auth (updates last used date of key)
	public Optional<String> findSecretKeyForApiKeyAuth(String apiKey){
		return findAccountKeyApiKeyAuth(apiKey, true)
				.map(accountKey -> accountKey.secretKey);
	}

	public Optional<String> getCurrentDatarouterAccountName(HttpServletRequest request){
		String apiKey = RequestTool.getParameterOrHeader(request, SecurityParameters.API_KEY);
		return findAccountKeyApiKeyAuth(apiKey, false)
				.map(accountKey -> accountKey.accountName);
	}

	public String getAccountNameForRequest(HttpServletRequest request){
		return getCurrentDatarouterAccountName(request)
				.orElseThrow();
	}

	public void deleteAllCredentialsForAccount(String accountName, Session session){
		datarouterAccountCredentialDao.deleteByAccountName(accountName);
		SecretOpReason reason = WebSecretOpReason.manualOp(session, "deleteAllCredentialsForAccount " + accountName);
		datarouterAccountSecretCredentialDao.scan()
				.include(secretCredential -> accountName.equals(secretCredential.getAccountName()))
				.each(secretCredential -> {
					secretService.deleteNamespaced(Optional.empty(), secretCredential.getSecretNamespace(),
							secretCredential.getKey().getSecretName(), reason);
					//not batching the DB deletes, in order to keep the secret and DB deletes close together
					datarouterAccountSecretCredentialDao.delete(secretCredential.getKey());
				});
	}

	public void createCredential(String accountName, String creatorUsername){
		//duplicate API keys are not allowed between regular and secret credentials
		DatarouterAccountCredential credential;
		do{
			credential = DatarouterAccountCredential.create(accountName, creatorUsername);
		}while(findAccountKeyApiKeyAuth(credential.getKey().getApiKey(), false).isPresent());
		datarouterAccountCredentialDao.insertOrBust(credential);
	}

	public void deleteCredential(String apiKey){
		datarouterAccountCredentialDao.delete(new DatarouterAccountCredentialKey(apiKey));
	}

	public DatarouterAccountSecretCredentialKeypairDto createSecretCredential(String accountName,
			String creatorUsername, SecretOpReason reason){
		String secretNamespace = secretNamespacer.getAppNamespace() + SECRET_NAMESPACE_SUFFIX;
		var credential = DatarouterAccountSecretCredential.create(secretNamespace, accountName, creatorUsername);
		datarouterAccountSecretCredentialDao.insertOrBust(credential);
		//duplicate API keys are not allowed between regular and secret credentials
		DatarouterAccountSecretCredentialKeypairDto keypair;
		do{
			keypair = DatarouterAccountSecretCredentialKeypairDto.create();
		}while(findAccountKeyApiKeyAuth(keypair.apiKey, false).isPresent());
		//delete from DB if creating the secret fails
		try{
			secretService.createNamespaced(secretNamespace, credential.getKey().getSecretName(), keypair, reason);
		}catch(RuntimeException e){
			datarouterAccountSecretCredentialDao.delete(credential.getKey());
			throw e;
		}
		return keypair;
	}

	public boolean deleteSecretCredential(String secretName, SecretOpReason reason){
		var key = new DatarouterAccountSecretCredentialKey(secretName);
		var databean = datarouterAccountSecretCredentialDao.get(key);
		if(databean == null){
			return false;
		}
		secretService.deleteNamespaced(Optional.empty(), databean.getSecretNamespace(), secretName, reason);
		datarouterAccountSecretCredentialDao.delete(key);
		return true;
	}

	public void setSecretCredentialActivation(String secretName, Boolean active){
		var key = new DatarouterAccountSecretCredentialKey(secretName);
		var databean = datarouterAccountSecretCredentialDao.get(key);
		databean.setActive(active);
		datarouterAccountSecretCredentialDao.updateIgnore(databean);
	}

	public Map<String,List<AccountCredentialDto>> getCredentialsByAccountName(Set<String> accountNames, ZoneId zoneId){
		return datarouterAccountCredentialDao.scanByAccountName(accountNames)
				.map(credential -> new AccountCredentialDto(credential, zoneId))
				.groupBy(credential -> credential.accountName);
	}

	public Map<String,List<SecretCredentialDto>> getSecretCredentialsByAccountName(Set<String> accountNames,
			ZoneId zoneId){
		return datarouterAccountSecretCredentialDao.scan()
				.include(secretCredential -> accountNames.contains(secretCredential.getAccountName()))
				.map(databean -> new SecretCredentialDto(databean, zoneId))
				.groupBy(dto -> dto.accountName);
	}

	private Optional<AccountKey> findAccountKeyApiKeyAuth(String apiKey, boolean shouldUpdate){
		var accountKey = secretCredentialAccountKeyByApiKey.get().get(apiKey);
		if(accountKey != null){
			if(shouldUpdate){
				datarouterAccountLastUsedDateService.updateLastUsedDateForSecretCredential(
						accountKey.getDatarouterAccountSecretCredentialKey(), accountKey.accountName);
			}
			return Optional.of(accountKey);
		}
		accountKey = credentialAccountKeyByApiKey.get().get(apiKey);
		if(accountKey != null){
			if(shouldUpdate){
				datarouterAccountLastUsedDateService.updateLastUsedDateForCredential(
						accountKey.getDatarouterAccountCredentialKey(), accountKey.accountName);
			}
			return Optional.of(accountKey);
		}
		return Optional.empty();
	}

	private void refreshCredentials(){
		credentialAccountKeyByApiKey.set(datarouterAccountCredentialDao.scan()
				//TODO active only, once active is added
				.toMap(databean -> databean.getKey().getApiKey(), AccountKey::new));
	}

	private void refreshSecretCredentials(){
		var oldApiKeyBySecretName = secretCredentialApiKeyBySecretName.get();
		var oldAccountKeyByApiKey = secretCredentialAccountKeyByApiKey.get();
		Map<String,String> newApiKeyBySecretName = new HashMap<>();
		Map<String,AccountKey> newAccountKeyByApiKey = new HashMap<>();
		datarouterAccountSecretCredentialDao.scan()
				.include(DatarouterAccountSecretCredential::getActive)
				.forEach(credential -> {
					String secretName = credential.getKey().getSecretName();
					String oldApiKey = oldApiKeyBySecretName.get(secretName);
					AccountKey oldAccountKey = oldAccountKeyByApiKey.get(oldApiKey);
					if(oldApiKey != null && oldAccountKey != null){
						//no change: use the current values for secretName, apiKey, accountKey
						newApiKeyBySecretName.put(secretName, oldApiKey);
						newAccountKeyByApiKey.put(oldApiKey, oldAccountKey);
					}else{
						//secret has not been read/cached yet, so do it now
						var reason = SecretOpReason.automatedOp(DatarouterAccountCredentialService.class
								.getSimpleName() + " caching");
						var keypair = readKeypair(credential, reason);
						newApiKeyBySecretName.put(secretName, keypair.apiKey);
						newAccountKeyByApiKey.put(keypair.apiKey, new AccountKey(keypair, credential));
					}
				});
		secretCredentialApiKeyBySecretName.set(newApiKeyBySecretName);
		secretCredentialAccountKeyByApiKey.set(newAccountKeyByApiKey);
	}

	private DatarouterAccountSecretCredentialKeypairDto readKeypair(DatarouterAccountSecretCredential secretCredential,
			SecretOpReason reason){
		return secretService.readNamespaced(secretCredential.getSecretNamespace(), secretCredential.getKey()
				.getSecretName(), DatarouterAccountSecretCredentialKeypairDto.class, reason);
	}

	private void refreshCaches(){
		refreshCredentials();
		refreshSecretCredentials();
	}

	private static class AccountKey{

		private final String apiKey;
		private final String secretKey;
		private final String accountName;
		private final String secretName;

		private AccountKey(DatarouterAccountCredential credential){
			this.apiKey = Require.notNull(credential.getKey().getApiKey());
			this.secretKey = Require.notNull(credential.getSecretKey());
			this.accountName = Require.notNull(credential.getAccountName());
			this.secretName = null;
		}

		private AccountKey(DatarouterAccountSecretCredentialKeypairDto keypair,
				DatarouterAccountSecretCredential credential){
			this.apiKey = Require.notNull(keypair.apiKey);
			this.secretKey = Require.notNull(keypair.secretKey);
			this.accountName = Require.notNull(credential.getAccountName());
			this.secretName = Require.notNull(credential.getKey().getSecretName());
		}

		DatarouterAccountCredentialKey getDatarouterAccountCredentialKey(){
			return new DatarouterAccountCredentialKey(apiKey);
		}

		DatarouterAccountSecretCredentialKey getDatarouterAccountSecretCredentialKey(){
			return new DatarouterAccountSecretCredentialKey(secretName);
		}

	}

	public static class DatarouterAccountSecretCredentialKeypairDto{

		public final String apiKey;
		public final String secretKey;

		public DatarouterAccountSecretCredentialKeypairDto(String apiKey, String secretKey){
			this.apiKey = apiKey;
			this.secretKey = secretKey;
		}

		public static DatarouterAccountSecretCredentialKeypairDto create(){
			return new DatarouterAccountSecretCredentialKeypairDto(PasswordTool.generateSalt(), PasswordTool
					.generateSalt());
		}

	}

	public static class SecretCredentialDto{

		public final String secretName;
		public final String accountName;
		public final String created;
		public final String creatorUsername;
		public final String lastUsed;
		public final Boolean active;

		public SecretCredentialDto(DatarouterAccountSecretCredential credential, ZoneId zoneId){
			this.secretName = credential.getKey().getSecretName();
			this.accountName = credential.getAccountName();
			this.created = credential.getCreatedDate(zoneId);
			this.creatorUsername = credential.getCreatorUsername();
			this.lastUsed = credential.getLastUsedDate(zoneId);
			this.active = credential.getActive();
		}

	}

}
