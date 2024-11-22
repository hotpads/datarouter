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

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.auth.session.Session;
import io.datarouter.auth.storage.account.DatarouterAccountDao;
import io.datarouter.auth.storage.account.DatarouterAccountKey;
import io.datarouter.auth.storage.account.credential.DatarouterAccountCredential;
import io.datarouter.auth.storage.account.credential.DatarouterAccountCredentialDao;
import io.datarouter.auth.storage.account.credential.DatarouterAccountCredentialKey;
import io.datarouter.auth.storage.account.credential.secret.DatarouterAccountSecretCredential;
import io.datarouter.auth.storage.account.credential.secret.DatarouterAccountSecretCredentialDao;
import io.datarouter.auth.storage.account.credential.secret.DatarouterAccountSecretCredentialKey;
import io.datarouter.auth.storage.account.permission.DatarouterAccountPermissionKey;
import io.datarouter.auth.util.PasswordTool;
import io.datarouter.auth.web.cache.DatarouterAccountPermissionKeysByPrefixCache;
import io.datarouter.auth.web.config.DatarouterAuthExecutors.DatarouterAccountCredentialCacheExecutor;
import io.datarouter.auth.web.config.DatarouterAuthSettingRoot;
import io.datarouter.auth.web.web.DatarouterAccountManagerHandler.AccountCredentialDto;
import io.datarouter.httpclient.dto.DatarouterAccountCredentialStatusDto;
import io.datarouter.httpclient.security.SecurityParameters;
import io.datarouter.scanner.Scanner;
import io.datarouter.secret.op.SecretOpConfig;
import io.datarouter.secret.op.SecretOpReason;
import io.datarouter.secret.service.SecretNamespacer;
import io.datarouter.secret.service.SecretService;
import io.datarouter.secretweb.service.WebSecretOpReason;
import io.datarouter.util.Require;
import io.datarouter.web.util.http.RequestTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterAccountCredentialService{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterAccountCredentialService.class);

	private static final String SECRET_NAMESPACE_SUFFIX = "drSecretCredentials/";
	//matches <url safe base64 character(s)>*<url safe base64 character(s)>
	private static final Pattern OBFUSCATED_API_KEY_PATTERN = Pattern
			.compile("([a-zA-Z0-9\\-_]+)\\*([a-zA-Z0-9\\-_]+)");

	private final DatarouterAccountDao datarouterAccountDao;
	private final DatarouterAccountCredentialDao datarouterAccountCredentialDao;
	private final DatarouterAccountSecretCredentialDao datarouterAccountSecretCredentialDao;
	private final DatarouterAccountPermissionKeysByPrefixCache datarouterAccountPermissionKeysByPrefixCache;
	private final DatarouterAccountLastUsedDateService datarouterAccountLastUsedDateService;
	private final SecretService secretService;
	private final SecretNamespacer secretNamespacer;

	private final AtomicReference<Map<String,AccountKey>> credentialAccountKeyByApiKey;
	private final AtomicReference<Map<String,String>> secretCredentialApiKeyBySecretName;
	private final AtomicReference<Map<String,AccountKey>> secretCredentialAccountKeyByApiKey;
	private final AtomicReference<Map<String,Instant>> mostRecentCreatedInstantByAccountName;

	@Inject
	public DatarouterAccountCredentialService(
			DatarouterAccountDao datarouterAccountDao,
			DatarouterAccountCredentialDao datarouterAccountCredentialDao,
			DatarouterAccountSecretCredentialDao datarouterAccountSecretCredentialDao,
			DatarouterAccountPermissionKeysByPrefixCache datarouterAccountPermissionKeysByPrefixCache,
			DatarouterAccountLastUsedDateService datarouterAccountLastUsedDateService,
			DatarouterAccountCredentialCacheExecutor executor,
			SecretService secretService,
			SecretNamespacer secretNamespacer,
			DatarouterAuthSettingRoot datarouterAuthSettingRoot){
		this.datarouterAccountDao = datarouterAccountDao;
		this.datarouterAccountCredentialDao = datarouterAccountCredentialDao;
		this.datarouterAccountSecretCredentialDao = datarouterAccountSecretCredentialDao;
		this.datarouterAccountPermissionKeysByPrefixCache = datarouterAccountPermissionKeysByPrefixCache;
		this.datarouterAccountLastUsedDateService = datarouterAccountLastUsedDateService;
		this.secretService = secretService;
		this.secretNamespacer = secretNamespacer;

		credentialAccountKeyByApiKey = new AtomicReference<>(new HashMap<>());
		secretCredentialApiKeyBySecretName = new AtomicReference<>(new HashMap<>());
		secretCredentialAccountKeyByApiKey = new AtomicReference<>(new HashMap<>());
		mostRecentCreatedInstantByAccountName = new AtomicReference<>(new HashMap<>());
		Duration accountRefreshFrequencyDuration = datarouterAuthSettingRoot.accountRefreshFrequencyDuration.get()
				.toJavaDuration();
		refreshCaches();
		executor.scheduleWithFixedDelay(
				this::refreshCaches,
				accountRefreshFrequencyDuration.getSeconds(),
				accountRefreshFrequencyDuration.getSeconds(),
				TimeUnit.SECONDS);
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

	public List<AccountLookupDto> lookupAccountName(String apiKey){
		Matcher matcher = OBFUSCATED_API_KEY_PATTERN.matcher(apiKey);
		if(matcher.matches()){
			String prefix = matcher.group(1);
			String suffix = matcher.group(2);
			return Scanner.of(credentialAccountKeyByApiKey.get().values())
					.append(secretCredentialAccountKeyByApiKey.get().values())
					.include(accountKey -> accountKey.apiKey.startsWith(prefix) && accountKey.apiKey.endsWith(suffix))
					.map(accountKey -> new AccountLookupDto(accountKey.accountName, accountKey.secretName))
					.list();
		}
		return findAccountKeyApiKeyAuth(apiKey, false)
				.map(accountKey -> new AccountLookupDto(accountKey.accountName, accountKey.secretName))
				.map(List::of)
				.orElseGet(List::of);
	}

	public DatarouterAccountCredentialStatusDto getCredentialStatusDto(HttpServletRequest request){
		String apiKey = RequestTool.getParameterOrHeader(request, SecurityParameters.API_KEY);
		AccountKey accountKey = findAccountKeyApiKeyAuth(apiKey, false).get();
		if(accountKey.secretName != null){
			var credential = datarouterAccountSecretCredentialDao.get(new DatarouterAccountSecretCredentialKey(
					accountKey.secretName));
			return new DatarouterAccountCredentialStatusDto(
					accountKey.accountName,
					accountKey.secretName,
					credential.getCreatedInstant(),
					shouldRotate(accountKey.accountName, credential.getCreatedInstant()),
					null,
					null);
		}
		var credential = datarouterAccountCredentialDao.get(new DatarouterAccountCredentialKey(apiKey));
		return new DatarouterAccountCredentialStatusDto(
				accountKey.accountName,
				null,//TODO figure out secure identifier for non-secret credentials
				credential.getCreatedInstant(),
				shouldRotate(accountKey.accountName, credential.getCreatedInstant()),
				null,
				null);

	}

	private boolean shouldRotate(String accountName, Instant currentCredentialCreated){
		return mostRecentCreatedInstantByAccountName.get().get(accountName).isAfter(currentCredentialCreated);
	}

	public Optional<String> getCurrentDatarouterAccountName(HttpServletRequest request){
		return findAccountNameForApiKey(RequestTool.getParameterOrHeader(request, SecurityParameters.API_KEY));
	}

	public String getAccountNameForRequest(HttpServletRequest request, String alreadyKnown){
		String redoLogic = getCurrentDatarouterAccountName(request)
				.orElseThrow();
		if(!redoLogic.equals(alreadyKnown)){
			logger.warn("redoLogic={} alreadyknown={} path={}", redoLogic, alreadyKnown, RequestTool.getPath(request));
		}
		return redoLogic;
	}

	public Optional<String> findAccountNameForApiKey(String apiKey){
		return findAccountKeyApiKeyAuth(apiKey, false)
				.map(accountKey -> accountKey.accountName);
	}

	public void deleteAllCredentialsForAccount(String accountName, Session session){
		datarouterAccountCredentialDao.deleteByAccountName(accountName);
		SecretOpReason reason = WebSecretOpReason.manualOp(session, "deleteAllCredentialsForAccount " + accountName);
		datarouterAccountSecretCredentialDao.scan()
				.include(secretCredential -> accountName.equals(secretCredential.getAccountName()))
				.each(secretCredential -> {
					deleteSecret(
							secretCredential.getSecretNamespace(),
							secretCredential.getKey().getSecretName(),
							reason);
					//not batching the DB deletes, in order to keep the secret and DB deletes close together
					datarouterAccountSecretCredentialDao.delete(secretCredential.getKey());
				});
	}

	public AccountKey createCredential(String accountName, String creatorUsername){
		//duplicate API keys are not allowed between regular and secret credentials
		DatarouterAccountCredential credential;
		do{
			credential = DatarouterAccountCredential.create(accountName, creatorUsername);
		}while(findAccountKeyApiKeyAuth(credential.getKey().getApiKey(), false).isPresent());
		datarouterAccountCredentialDao.insertOrBust(credential);
		return new AccountKey(credential);
	}

	public void deleteCredential(String apiKey){
		datarouterAccountCredentialDao.delete(new DatarouterAccountCredentialKey(apiKey));
	}

	public void setCredentialActivation(String apiKey, Boolean active){
		var key = new DatarouterAccountCredentialKey(apiKey);
		var databean = datarouterAccountCredentialDao.get(key);
		databean.setActive(active);
		datarouterAccountCredentialDao.updateIgnore(databean);
	}

	public AccountKey createSecretCredential(String accountName,
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
			SecretOpConfig config = SecretOpConfig.builder(reason)
					.useManualNamespace(secretNamespace)
					.build();
			secretService.create(credential.getKey().getSecretName(), keypair, config);
		}catch(RuntimeException e){
			datarouterAccountSecretCredentialDao.delete(credential.getKey());
			throw e;
		}
		return new AccountKey(keypair, credential);
	}

	public boolean deleteSecretCredential(String secretName, SecretOpReason reason){
		var key = new DatarouterAccountSecretCredentialKey(secretName);
		var databean = datarouterAccountSecretCredentialDao.get(key);
		if(databean == null){
			return false;
		}
		deleteSecret(databean.getSecretNamespace(), secretName, reason);
		datarouterAccountSecretCredentialDao.delete(key);
		return true;
	}

	public void deleteOrphanedCredentials(){
		Set<String> currentAccountNames = datarouterAccountDao.scanKeys()
				.map(DatarouterAccountKey::getAccountName)
				.collect(Collectors.toSet());
		Scanner.of(credentialAccountKeyByApiKey.get().values())
				.append(secretCredentialAccountKeyByApiKey.get().values())
				.include(accountKey -> !currentAccountNames.contains(accountKey.accountName))
				.forEach(accountKey -> {
					if(accountKey.secretName != null){
						deleteSecretCredential(accountKey.secretName, SecretOpReason.automatedOp(
								"deleteOrphanedCredentials"));
					}else{
						deleteCredential(accountKey.apiKey);
					}
				});
	}

	public void setSecretCredentialActivation(String secretName, Boolean active){
		var key = new DatarouterAccountSecretCredentialKey(secretName);
		var databean = datarouterAccountSecretCredentialDao.get(key);
		databean.setActive(active);
		datarouterAccountSecretCredentialDao.updateIgnore(databean);
	}

	public Map<String,List<AccountCredentialDto>> getCredentialsByAccountName(Set<String> accountNames, ZoneId zoneId){
		return datarouterAccountCredentialDao.scanByAccountNames(accountNames)
				.map(credential -> new AccountCredentialDto(credential, zoneId))
				.groupBy(AccountCredentialDto::accountName);
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

	private HashMap<String,Instant> refreshCredentials(){
		HashMap<String,Instant> mostRecentCreatedInstantByAccountName = new HashMap<>();
		credentialAccountKeyByApiKey.set(datarouterAccountCredentialDao.scan()
				.include(DatarouterAccountCredential::getActive)
				.each(credential -> mostRecentCreatedInstantByAccountName.merge(credential.getAccountName(), credential
						.getCreatedInstant(), DatarouterAccountCredentialService::maxInstant))
				.toMap(databean -> databean.getKey().getApiKey(), AccountKey::new));
		return mostRecentCreatedInstantByAccountName;
	}

	private HashMap<String,Instant> refreshSecretCredentials(){
		HashMap<String,Instant> mostRecentCreatedInstantByAccountName = new HashMap<>();
		var oldApiKeyBySecretName = secretCredentialApiKeyBySecretName.get();
		var oldAccountKeyByApiKey = secretCredentialAccountKeyByApiKey.get();
		Map<String,String> newApiKeyBySecretName = new HashMap<>();
		Map<String,AccountKey> newAccountKeyByApiKey = new HashMap<>();
		datarouterAccountSecretCredentialDao.scan()
				.include(DatarouterAccountSecretCredential::getActive)
				.each(credential -> mostRecentCreatedInstantByAccountName.merge(credential.getAccountName(), credential
						.getCreatedInstant(), DatarouterAccountCredentialService::maxInstant))
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
		return mostRecentCreatedInstantByAccountName;
	}

	private DatarouterAccountSecretCredentialKeypairDto readKeypair(DatarouterAccountSecretCredential secretCredential,
			SecretOpReason reason){
		SecretOpConfig config = SecretOpConfig.builder(reason)
				.useManualNamespace(secretCredential.getSecretNamespace())
				.build();
		return secretService.read(
				secretCredential.getKey().getSecretName(),
				DatarouterAccountSecretCredentialKeypairDto.class,
				config);
	}

	private void deleteSecret(String secretNamespace, String secretName, SecretOpReason reason){
		var config = SecretOpConfig.builder(reason)
				.useManualNamespace(secretNamespace)
				.build();
		secretService.delete(secretName, config);
	}

	private void refreshCaches(){
		HashMap<String,Instant> combinedMostRecentRefreshes = refreshCredentials();
		HashMap<String,Instant> mostRecentSecretRefreshes = refreshSecretCredentials();
		mostRecentSecretRefreshes.forEach((accountName, created) -> combinedMostRecentRefreshes.merge(accountName,
				created, DatarouterAccountCredentialService::maxInstant));
		mostRecentCreatedInstantByAccountName.set(combinedMostRecentRefreshes);

	}

	private static Instant maxInstant(Instant i1, Instant i2){
		return i1.isAfter(i2) ? i1 : i2;
	}

	public static class AccountKey{

		public final String apiKey;
		public final String secretKey;
		public final String accountName;
		public final String secretName;

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

		public DatarouterAccountSecretCredentialKeypairDto getDatarouterAccountSecretCredentialKeypairDto(){
			return new DatarouterAccountSecretCredentialKeypairDto(apiKey, secretKey);
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

	public static class AccountLookupDto{

		public final String accountName;
		public final String secretName;

		private AccountLookupDto(String accountName, String secretName){
			this.accountName = accountName;
			this.secretName = secretName;
		}

		public static AccountLookupDto empty(){
			return new AccountLookupDto(null, null);
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
