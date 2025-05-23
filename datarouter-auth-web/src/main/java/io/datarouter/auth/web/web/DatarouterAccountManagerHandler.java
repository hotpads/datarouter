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

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.auth.config.DatarouterAuthPaths;
import io.datarouter.auth.service.CurrentUserSessionInfoService;
import io.datarouter.auth.session.Session;
import io.datarouter.auth.storage.account.DatarouterAccount;
import io.datarouter.auth.storage.account.DatarouterAccountDao;
import io.datarouter.auth.storage.account.DatarouterAccountKey;
import io.datarouter.auth.storage.account.credential.DatarouterAccountCredential;
import io.datarouter.auth.storage.account.permission.DatarouterAccountPermission;
import io.datarouter.auth.storage.account.permission.DatarouterAccountPermissionDao;
import io.datarouter.auth.storage.account.permission.DatarouterAccountPermissionKey;
import io.datarouter.auth.web.config.DatarouterAuthFiles;
import io.datarouter.auth.web.config.metrics.DatarouterAccountMetrics;
import io.datarouter.auth.web.service.AccountCallerTypeRegistry2;
import io.datarouter.auth.web.service.DatarouterAccountAvailableEndpointsProvider;
import io.datarouter.auth.web.service.DatarouterAccountCredentialService;
import io.datarouter.auth.web.service.DatarouterAccountCredentialService.AccountLookupDto;
import io.datarouter.auth.web.service.DatarouterAccountCredentialService.DatarouterAccountSecretCredentialKeypairDto;
import io.datarouter.auth.web.service.DatarouterAccountCredentialService.SecretCredentialDto;
import io.datarouter.auth.web.service.DatarouterAccountDeleteAction;
import io.datarouter.httpclient.endpoint.caller.CallerType;
import io.datarouter.httpclient.endpoint.param.RequestBody;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDtoBuilder;
import io.datarouter.instrumentation.metric.MetricLinkBuilder;
import io.datarouter.scanner.Scanner;
import io.datarouter.secretweb.service.WebSecretOpReason;
import io.datarouter.storage.config.properties.DatarouterServerTypeSupplier;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.types.MilliTime;
import io.datarouter.util.Require;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.dispatcher.ApiKeyPredicate;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.react.bootstrap4.Bootstrap4ReactPageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJs;
import jakarta.inject.Inject;

public class DatarouterAccountManagerHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterAccountManagerHandler.class);

	public static final String CHANGELOG_TYPE = "DatarouterAccount";

	@Inject
	private DatarouterAccountDao datarouterAccountDao;
	@Inject
	private DatarouterAccountPermissionDao datarouterAccountPermissionDao;
	@Inject
	private DatarouterAccountCredentialService acccountCredentialService;
	@Inject
	private DatarouterServerTypeSupplier serverType;
	@Inject
	private DatarouterAuthFiles files;
	@Inject
	private DatarouterAccountAvailableEndpointsProvider datarouterAccountAvailableEndpointsProvider;
	@Inject
	private Bootstrap4ReactPageFactory reactPageFactory;
	@Inject
	private ChangelogRecorder changelogRecorder;
	@Inject
	private MetricLinkBuilder metricLinkBuilder;
	@Inject
	private CurrentUserSessionInfoService currentSessionInfoService;
	@Inject
	private DatarouterAccountDeleteAction datarouterAccountDeleteAction;
	@Inject
	private AccountCallerTypeRegistry2 callerTypeRegistry;
	@Inject
	private DatarouterAuthPaths datarouterAuthPaths;
	@Inject
	private DatarouterAccountMetrics accountMetrics;

	@Handler(defaultHandler = true)
	public Mav index(){
		return reactPageFactory.startBuilder(request)
				.withTitle("Datarouter Account Manager")
				.withRequires(DatarouterWebRequireJs.SORTTABLE)
				.withReactScript(files.js.accountManagerJsx)
				.withJsStringConstant("REACT_BASE_PATH", request.getContextPath()
						+ datarouterAuthPaths.datarouter.accountManager.toSlashedString() + "/")
				.withJsStringConstant("RENAMER_PATH", request.getContextPath()
						+ datarouterAuthPaths.datarouter.accounts.renameAccounts.toSlashedString())
				.withJsStringConstant("CALLER_TYPE_PATH", request.getContextPath()
						+ datarouterAuthPaths.datarouter.accounts.updateCallerType.toSlashedString())
				.buildMav();
	}

	@Handler
	public List<DatarouterAccountDetailsDto> list(){
		return datarouterAccountDao.scan()
				.listTo(this::getDetailsForAccounts);
	}

	@Handler
	public DatarouterAccountDetailsDto getDetails(String accountName){
		return getDetailsForAccountName(accountName);
	}

	@Handler
	public DatarouterAccountDetailsDto add(String accountName, String callerType){
		Require.isFalse(accountName.isEmpty());
		String creator = getSessionInfo().getRequiredSession().getUsername();
		var account = new DatarouterAccount(accountName, MilliTime.now(), creator);
		account.setCallerType(callerType);
		datarouterAccountDao.put(account);
		logAndRecordAction(accountName, "add");
		return getDetailsForAccounts(List.of(account)).getFirst();
	}

	@Handler
	public List<String> getAvailableCallerTypes(){
		return callerTypeRegistry.get().stream()
				.map(ReflectionTool::create)
				.map(CallerType::getName)
				.sorted()
				.toList();
	}

	@Handler
	public DatarouterAccountDetailsDto toggleUserMappings(String accountName){
		return modifyAccount("toggleUserMappings", accountName, DatarouterAccount::toggleUserMappings);
	}

	@Handler
	public void delete(String accountName){
		DatarouterAccountPermissionKey prefix = new DatarouterAccountPermissionKey(accountName);
		datarouterAccountPermissionDao.deleteWithPrefix(prefix);
		acccountCredentialService.deleteAllCredentialsForAccount(accountName, getSessionInfo().getRequiredSession());
		DatarouterAccountKey accountKey = new DatarouterAccountKey(accountName);
		DatarouterAccount account = datarouterAccountDao.get(accountKey);
		datarouterAccountDeleteAction.onDelete(account);
		datarouterAccountDao.delete(accountKey);
		logAndRecordAction(accountName, "delete");
	}

	@Handler
	public List<AccountLookupDto> lookupAccount(String apiKey){
		return acccountCredentialService.lookupAccountName(apiKey);
	}

	@Handler
	public DatarouterAccountDetailsDto addCredential(String accountName){
		Require.isFalse(accountName.isEmpty());
		String creatorUsername = getSessionInfo().getRequiredSession().getUsername();
		var accountKey = acccountCredentialService.createCredential(accountName, creatorUsername);
		logAndRecordAction(accountName, "addCredential", getCredentialNote(accountKey.apiKey));
		return getDetailsForAccountName(accountName);
	}

	@Handler
	public DatarouterAccountDetailsDto deleteCredential(String apiKey, String accountName){
		acccountCredentialService.deleteCredential(apiKey);
		logAndRecordAction(accountName, "deleteCredential", getCredentialNote(apiKey));
		return getDetailsForAccountName(accountName);
	}

	@Handler
	public DatarouterAccountDetailsAndKeypairDto addSecretCredential(String accountName){
		Require.isFalse(accountName.isEmpty());
		Session session = getSessionInfo().getRequiredSession();
		String creatorUsername = session.getUsername();
		var secretOpReason = WebSecretOpReason.manualOp(session, getClass().getSimpleName());
		var accountKey = acccountCredentialService.createSecretCredential(accountName, creatorUsername, secretOpReason);
		logAndRecordAction(accountName, "addSecretCredential", getSecretCredentialNote(accountKey.secretName, accountKey
				.apiKey));
		return new DatarouterAccountDetailsAndKeypairDto(getDetailsForAccountName(accountName), accountKey
				.getDatarouterAccountSecretCredentialKeypairDto());
	}

	@Handler
	public DatarouterAccountDetailsDto deleteSecretCredential(String secretName, String accountName){
		var secretOpReason = WebSecretOpReason.manualOp(getSessionInfo().getRequiredSession(), getClass()
				.getSimpleName());
		acccountCredentialService.deleteSecretCredential(secretName, secretOpReason);
		logAndRecordAction(accountName, "deleteSecretCredential", getSecretCredentialNote(secretName));
		return getDetailsForAccountName(accountName);
	}

	@Handler
	public DatarouterAccountDetailsDto setCredentialActivation(@RequestBody SetCredentialActivationDto dto){
		Require.notBlank(dto.accountName);
		Require.notNull(dto.active);
		String active = dto.active ? "Active" : "Inactive";
		if(dto.secretName != null && StringTool.notEmptyNorWhitespace(dto.secretName)){
			acccountCredentialService.setSecretCredentialActivation(dto.secretName, dto.active);
			logAndRecordAction(dto.accountName, "setSecretCredential" + active, getSecretCredentialNote(dto
					.secretName));
		}else if(dto.apiKey != null && StringTool.notEmptyNorWhitespace(dto.apiKey)){
			acccountCredentialService.setCredentialActivation(dto.apiKey, dto.active);
			logAndRecordAction(dto.accountName, "setCredential" + active, getCredentialNote(dto.apiKey));
		}else{
			throw new RuntimeException("apiKey or secretName is required");
		}
		return getDetails(dto.accountName);
	}

	@Handler
	public DatarouterAccountDetailsDto updateReferrer(String accountName, String referrer){
		return modifyAccount("updateReferrer", accountName, account -> account.setReferrer(referrer));
	}

	@Handler
	public List<String> getAvailableEndpoints(){
		List<String> availableEndpoints = new ArrayList<>();
		availableEndpoints.addAll(datarouterAccountAvailableEndpointsProvider.getAvailableEndpoints());
		availableEndpoints.sort(Comparator.naturalOrder());
		availableEndpoints.addFirst(DatarouterAccountPermissionKey.ALL_ENDPOINTS);
		return availableEndpoints;
	}

	@Handler
	public DatarouterAccountDetailsDto addPermission(String accountName, String endpoint){
		datarouterAccountPermissionDao.put(new DatarouterAccountPermission(accountName, endpoint));
		logAndRecordAction(accountName, "addPermission", Optional.of(endpoint));
		return getDetails(accountName);
	}

	@Handler
	public DatarouterAccountDetailsDto deletePermission(String accountName, String endpoint){
		datarouterAccountPermissionDao.delete(new DatarouterAccountPermissionKey(accountName, endpoint));
		logAndRecordAction(accountName, "deletePermission", Optional.of(endpoint));
		return getDetails(accountName);
	}

	@Handler
	public boolean isServerTypeDev(){
		return StringTool.equalsCaseInsensitive(serverType.getServerTypeString(), ServerType.DEV.getPersistentString());
	}

	private DatarouterAccountDetailsDto modifyAccount(String action, String accountName,
			Consumer<DatarouterAccount> editor){
		var key = new DatarouterAccountKey(Require.notBlank(accountName));
		DatarouterAccount account = datarouterAccountDao.get(key);
		editor.accept(account);
		datarouterAccountDao.put(account);
		logAndRecordAction(accountName, action);
		return getDetailsForAccountName(accountName);
	}

	private List<DatarouterAccountDetailsDto> getDetailsForAccounts(List<DatarouterAccount> accounts){
		ZoneId zoneId = currentSessionInfoService.getZoneId(request);
		Set<String> accountNames = Scanner.of(accounts)
				.map(DatarouterAccount::getKey)
				.map(DatarouterAccountKey::getAccountName)
				.collect(HashSet::new);

		var credentialsByAccountName = acccountCredentialService.getCredentialsByAccountName(accountNames, zoneId);
		var secretCredentialsByAccountName = acccountCredentialService.getSecretCredentialsByAccountName(accountNames,
				zoneId);
		var permissionsByAccountName = Scanner.of(accountNames)
				.map(DatarouterAccountPermissionKey::new)
				.listTo(datarouterAccountPermissionDao::scanKeysWithPrefixes)
				.map(TextPermissionDto::new)
				.groupBy(permission -> permission.accountName);

		return Scanner.of(accounts)
				.map(account -> new AccountDto(account, zoneId))
				.map(account -> getDetailsForAccount(
						account,
						credentialsByAccountName.get(account.accountName),
						secretCredentialsByAccountName.get(account.accountName),
						permissionsByAccountName.get(account.accountName)))
				.list();
	}

	private DatarouterAccountDetailsDto getDetailsForAccount(
			AccountDto account,
			List<AccountCredentialDto> credentials,
			List<SecretCredentialDto> secretCredentials,
			List<TextPermissionDto> permissions){
		String metricName = accountMetrics.getAccountMetricName(account.accountName());
		String metricLink = metricLinkBuilder.exactMetricLink(metricName);
		return new DatarouterAccountDetailsDto(
				account,
				credentials,
				secretCredentials,
				permissions,
				metricLink);
	}

	public DatarouterAccountDetailsDto getDetailsForAccountName(String accountName){
		DatarouterAccount account = datarouterAccountDao.get(new DatarouterAccountKey(accountName));
		return getDetailsForAccounts(List.of(account)).getFirst();
	}

	private void logAndRecordAction(String account, String action){
		logAndRecordAction(account, action, Optional.empty());
	}

	private void logAndRecordAction(String account, String action, Optional<String> note){
		var username = getSessionInfo().getNonEmptyUsernameOrElse("unknown");
		logger.warn("account={} action={} by={} note: {}", account, action, username, note.orElse("none"));
		var changelogBuilder = new DatarouterChangelogDtoBuilder(CHANGELOG_TYPE, account, action, username);
		note.ifPresent(changelogBuilder::withNote);
		changelogRecorder.record(changelogBuilder.build());
	}

	private static Optional<String> getCredentialNote(String apiKey){
		return Optional.of("apiKey=" + ApiKeyPredicate.obfuscate(apiKey));
	}

	private static Optional<String> getSecretCredentialNote(String secretName){
		return Optional.of("secretName=" + secretName);
	}

	private static Optional<String> getSecretCredentialNote(String secretName, String apiKey){
		return Optional.of(getSecretCredentialNote(secretName).get() + " " + getCredentialNote(apiKey).get());
	}

	public record DatarouterAccountDetailsAndKeypairDto(
			DatarouterAccountDetailsDto details,
			DatarouterAccountSecretCredentialKeypairDto keypair){
	}

	public record DatarouterAccountDetailsDto(
			AccountDto account,
			List<AccountCredentialDto> credentials,
			List<SecretCredentialDto> secretCredentials,
			List<TextPermissionDto> permissions,
			String metricLink,
			String error){

		public DatarouterAccountDetailsDto(
				AccountDto account,
				List<AccountCredentialDto> credentials,
				List<SecretCredentialDto> secretCredentials,
				List<TextPermissionDto> permissions,
				String metricLink){
			this(account,
					Objects.requireNonNullElseGet(credentials, List::of),
					Objects.requireNonNullElseGet(secretCredentials, List::of),
					Objects.requireNonNullElseGet(permissions, List::of),
					metricLink,
					null);
		}

		public DatarouterAccountDetailsDto(String error){
			this(null, null, null, null, null, error);
		}

	}

	public record AccountDto(
			String accountName,
			String created,
			String creator,
			String lastUsed,
			Boolean enableUserMappings,
			String callerType,
			String referrer,
			long lastUsedMs){

		public AccountDto(DatarouterAccount account, ZoneId zoneId){
			this(account.getKey().getAccountName(),
					account.getCreatedDate(zoneId),
					account.getCreator(),
					account.getLastUsedDate(zoneId),
					account.getEnableUserMappings(),
					account.getCallerType(),
					account.getReferrer(),
					Optional.ofNullable(account.getLastUsed())
							.map(MilliTime::toEpochMilli)
							.orElse(0L));
		}

	}

	public record AccountCredentialDto(
			String apiKey,
			String secretKey,
			String accountName,
			String created,
			String creatorUsername,
			String lastUsed,
			Boolean active){

		public AccountCredentialDto(
				DatarouterAccountCredential credential,
				ZoneId zoneId){
			this(credential.getKey().getApiKey(),
					credential.getSecretKey(),
					credential.getAccountName(),
					credential.getCreatedDate(zoneId),
					credential.getCreatorUsername(),
					credential.getLastUsedDate(zoneId),
					credential.getActive());
		}

	}

	public record TextPermissionDto(
			String accountName,
			String endpoint){

		public TextPermissionDto(DatarouterAccountPermissionKey permission){
			this(permission.getAccountName(), permission.getEndpoint());
		}

	}

	public record SetCredentialActivationDto(
			String apiKey,
			String secretName,
			Boolean active,
			String accountName){
	}

}
