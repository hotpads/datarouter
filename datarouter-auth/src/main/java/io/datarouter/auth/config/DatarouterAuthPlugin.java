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
package io.datarouter.auth.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.datarouter.auth.service.AccountCallerTypeRegistry2;
import io.datarouter.auth.service.CopyUserListener;
import io.datarouter.auth.service.CopyUserListener.DefaultCopyUserListener;
import io.datarouter.auth.service.DatarouterAccountDailyDigest;
import io.datarouter.auth.service.DatarouterAccountDeleteAction;
import io.datarouter.auth.service.DatarouterDefaultAccountKeysDailyDigest;
import io.datarouter.auth.service.DatarouterDefaultUserDailyDigest;
import io.datarouter.auth.service.DatarouterPermissionRequestUserInfo;
import io.datarouter.auth.service.DatarouterUserInfo;
import io.datarouter.auth.service.DefaultDatarouterAccountKeys;
import io.datarouter.auth.service.DefaultDatarouterAccountKeysSupplier;
import io.datarouter.auth.service.DefaultDatarouterUserPassword;
import io.datarouter.auth.service.DefaultDatarouterUserPasswordSupplier;
import io.datarouter.auth.service.PermissionRequestDailyDigest;
import io.datarouter.auth.service.PermissionRequestUserInfo;
import io.datarouter.auth.service.UserInfo;
import io.datarouter.auth.service.deprovisioning.DatarouterUserDeprovisioningStrategy;
import io.datarouter.auth.service.deprovisioning.UserDeprovisioningStrategy;
import io.datarouter.auth.storage.account.BaseDatarouterAccountCredentialDao;
import io.datarouter.auth.storage.account.BaseDatarouterAccountDao;
import io.datarouter.auth.storage.account.BaseDatarouterAccountSecretCredentialDao;
import io.datarouter.auth.storage.account.DatarouterAccountCredentialDao;
import io.datarouter.auth.storage.account.DatarouterAccountCredentialDao.DatarouterAccountCredentialDaoParams;
import io.datarouter.auth.storage.account.DatarouterAccountDao;
import io.datarouter.auth.storage.account.DatarouterAccountDao.DatarouterAccountDaoParams;
import io.datarouter.auth.storage.account.DatarouterAccountSecretCredentialDao;
import io.datarouter.auth.storage.account.DatarouterAccountSecretCredentialDao.DatarouterAccountSecretCredentialDaoParams;
import io.datarouter.auth.storage.accountpermission.BaseDatarouterAccountPermissionDao;
import io.datarouter.auth.storage.accountpermission.DatarouterAccountPermissionDao;
import io.datarouter.auth.storage.accountpermission.DatarouterAccountPermissionDao.DatarouterAccountPermissionDaoParams;
import io.datarouter.auth.storage.deprovisioneduser.DeprovisionedUserDao;
import io.datarouter.auth.storage.deprovisioneduser.DeprovisionedUserDao.DeprovisionedUserDaoParams;
import io.datarouter.auth.storage.permissionrequest.DatarouterPermissionRequestDao;
import io.datarouter.auth.storage.permissionrequest.DatarouterPermissionRequestDao.DatarouterPermissionRequestDaoParams;
import io.datarouter.auth.storage.user.DatarouterUserDao;
import io.datarouter.auth.storage.user.DatarouterUserDao.DatarouterUserDaoParams;
import io.datarouter.auth.storage.useraccountmap.BaseDatarouterUserAccountMapDao;
import io.datarouter.auth.storage.useraccountmap.DatarouterUserAccountMapDao;
import io.datarouter.auth.storage.useraccountmap.DatarouterUserAccountMapDao.DatarouterUserAccountMapDaoParams;
import io.datarouter.auth.storage.userhistory.DatarouterUserHistoryDao;
import io.datarouter.auth.storage.userhistory.DatarouterUserHistoryDao.DatarouterUserHistoryDaoParams;
import io.datarouter.auth.web.DatarouterDocumentationRouteSet;
import io.datarouter.httpclient.endpoint.caller.CallerType;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.job.config.DatarouterJobRouteSet;
import io.datarouter.plugin.PluginConfigKey;
import io.datarouter.plugin.PluginConfigValue;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.navigation.AppNavBarCategory;
import io.datarouter.web.navigation.DatarouterNavBarCategory;
import io.datarouter.web.user.BaseDatarouterSessionDao;
import io.datarouter.web.user.DatarouterSessionDao;
import io.datarouter.web.user.authenticate.saml.BaseDatarouterSamlDao;
import io.datarouter.web.user.authenticate.saml.DatarouterSamlDao;
import io.datarouter.web.user.authenticate.saml.DatarouterSamlDao.DatarouterSamlDaoParams;

public class DatarouterAuthPlugin extends BaseWebPlugin{

	private static final DatarouterAuthPaths PATHS = new DatarouterAuthPaths();

	private final Class<? extends UserDeprovisioningStrategy> userDeprovisioningStrategyClass;
	private final Class<? extends CopyUserListener> copyUserListenerClass;
	private final String defaultDatarouterUserPassword;
	private final String defaultApiKey;
	private final String defaultSecretKey;
	private final Optional<Class<? extends DatarouterAccountDeleteAction>> datarouterAccountDeleteAction;
	private final List<Class<? extends CallerType>> callerTypes2;

	private DatarouterAuthPlugin(
			boolean enableUserAuth,
			DatarouterAuthDaoModule daosModuleBuilder,
			Class<? extends UserDeprovisioningStrategy> userDeprovisioningStrategyClass,
			Class<? extends CopyUserListener> copyUserListenerClass,
			String defaultDatarouterUserPassword,
			String defaultApiKey,
			String defaultSecretKey,
			Optional<Class<? extends DatarouterAccountDeleteAction>> datarouterAccountDeleteAction,
			List<Class<? extends CallerType>> callerTypes2,
			Map<PluginConfigKey<?>,Class<? extends PluginConfigValue<?>>> configs){
		this.userDeprovisioningStrategyClass = userDeprovisioningStrategyClass;
		this.copyUserListenerClass = copyUserListenerClass;
		this.defaultDatarouterUserPassword = defaultDatarouterUserPassword;
		this.defaultApiKey = defaultApiKey;
		this.defaultSecretKey = defaultSecretKey;
		this.datarouterAccountDeleteAction = datarouterAccountDeleteAction;
		this.callerTypes2 = callerTypes2;

		if(enableUserAuth){
			addAppListener(DatarouterUserConfigAppListener.class);
			addAppNavBarItem(AppNavBarCategory.ADMIN, PATHS.admin.viewUsers, "View Users");
			addAppNavBarItem(AppNavBarCategory.USER, PATHS.admin.editUser, "Edit User");
			addAppNavBarItem(AppNavBarCategory.USER, PATHS.permissionRequest, "Permission Request");
			addDynamicNavBarItem(CreateUserNavBarItem.class);
			addRouteSetOrdered(DatarouterAuthRouteSet.class, DatarouterJobRouteSet.class);
		}

		addAppListener(DatarouterAccountConfigAppListener.class);
		addDatarouterNavBarItem(DatarouterNavBarCategory.KEYS, PATHS.datarouter.accountManager, "Account Manager");
		addDatarouterNavBarItem(DatarouterNavBarCategory.KEYS, PATHS.datarouter.accounts.renameAccounts,
				"Account Renamer");
		addDatarouterNavBarItem(DatarouterNavBarCategory.KEYS, PATHS.datarouter.accounts.updateCallerType,
				"Account Update Caller Type");

		addAppNavBarItem(AppNavBarCategory.ADMIN, PATHS.userDeprovisioning, "User Deprovisioning");
		addDynamicNavBarItem(ApiDocsNavBarItem.class);
		addRouteSet(DatarouterAccountApiRouteSet.class);
		addRouteSet(DatarouterAccountRouteSet.class);
		addRouteSet(DatarouterDocumentationRouteSet.class);
		addRouteSet(UserDeprovisioningRouteSet.class);
		addSettingRoot(DatarouterAuthSettingRoot.class);
		addPluginEntry(BaseTriggerGroup.KEY, DatarouterAuthTriggerGroup.class);
		setDaosModule(daosModuleBuilder);
		addDatarouterGithubDocLink("datarouter-auth");
		addDailyDigest(PermissionRequestDailyDigest.class);
		addDailyDigest(DatarouterAccountDailyDigest.class);
		addDailyDigest(DatarouterDefaultAccountKeysDailyDigest.class);
		addDailyDigest(DatarouterDefaultUserDailyDigest.class);
		configs.forEach(this::addPluginEntry);
	}

	@Override
	protected void configure(){
		bindActual(BaseDatarouterSessionDao.class, DatarouterSessionDao.class);
		bindActual(BaseDatarouterAccountDao.class, DatarouterAccountDao.class);
		bindActual(BaseDatarouterAccountCredentialDao.class, DatarouterAccountCredentialDao.class);
		bindActual(BaseDatarouterAccountSecretCredentialDao.class, DatarouterAccountSecretCredentialDao.class);
		bindActual(BaseDatarouterAccountPermissionDao.class, DatarouterAccountPermissionDao.class);
		bindActual(BaseDatarouterUserAccountMapDao.class, DatarouterUserAccountMapDao.class);
		bindActual(BaseDatarouterSamlDao.class, DatarouterSamlDao.class);

		bind(UserDeprovisioningStrategy.class).to(userDeprovisioningStrategyClass);
		bindActual(CopyUserListener.class, copyUserListenerClass);
		bindActualInstance(DefaultDatarouterUserPasswordSupplier.class,
				new DefaultDatarouterUserPassword(defaultDatarouterUserPassword));
		bindActualInstance(DefaultDatarouterAccountKeysSupplier.class,
				new DefaultDatarouterAccountKeys(defaultApiKey, defaultSecretKey));
		datarouterAccountDeleteAction.ifPresent(clazz -> {
			bind(DatarouterAccountDeleteAction.class).to(clazz);
		});
		bindActualInstance(AccountCallerTypeRegistry2.class, new AccountCallerTypeRegistry2(callerTypes2));
	}

	public static class DatarouterAuthPluginBuilder{

		private final boolean enableUserAuth;
		private final List<ClientId> defaultClientId;

		private Map<PluginConfigKey<?>,Class<? extends PluginConfigValue<?>>> configs = new HashMap<>();

		private Class<? extends UserDeprovisioningStrategy> userDeprovisioningStrategyClass =
				DatarouterUserDeprovisioningStrategy.class;
		private Class<? extends CopyUserListener> copyUserListenerClass = DefaultCopyUserListener.class;
		private String defaultDatarouterUserPassword = "";
		private String defaultApiKey = "";
		private String defaultSecretKey = "";
		private Optional<Class<? extends DatarouterAccountDeleteAction>> datarouterAccountDeleteAction = Optional
				.empty();
		private List<Class<? extends CallerType>> callerTypes2 = new ArrayList<>();

		public DatarouterAuthPluginBuilder(
				boolean enableUserAuth,
				List<ClientId> defaultClientId,
				String defaultDatarouterUserPassword,
				String defaultApiKey,
				String defaultSecretKey){
			this.enableUserAuth = enableUserAuth;
			this.defaultClientId = defaultClientId;
			this.defaultDatarouterUserPassword = defaultDatarouterUserPassword;
			this.defaultApiKey = defaultApiKey;
			this.defaultSecretKey = defaultSecretKey;

			// defaults
			configs.put(PermissionRequestUserInfo.KEY, DatarouterPermissionRequestUserInfo.class);
			configs.put(UserInfo.KEY, DatarouterUserInfo.class);
		}

		public DatarouterAuthPluginBuilder setUserInfoClass(Class<? extends UserInfo> userInfoClass){
			configs.put(UserInfo.KEY, userInfoClass);
			return this;
		}

		public DatarouterAuthPluginBuilder setUserDeprovisioningStrategyClass(
				Class<? extends UserDeprovisioningStrategy> userDeprovisioningStrategyClass){
			this.userDeprovisioningStrategyClass = userDeprovisioningStrategyClass;
			return this;
		}

		public DatarouterAuthPluginBuilder setCopyUserListenerClass(
				Class<? extends CopyUserListener> copyUserListenerClass){
			this.copyUserListenerClass = copyUserListenerClass;
			return this;
		}

		public DatarouterAuthPluginBuilder setDatarouterAccountDeleteAction(
				Class<? extends DatarouterAccountDeleteAction> datarouterAccountDeleteAction){
			this.datarouterAccountDeleteAction = Optional.of(datarouterAccountDeleteAction);
			return this;
		}

		public DatarouterAuthPluginBuilder setPermissionRequestUserInfo(
				Class<? extends PermissionRequestUserInfo> permissionRequestUserInfo){
			configs.put(PermissionRequestUserInfo.KEY, permissionRequestUserInfo);
			return this;
		}

		public DatarouterAuthPluginBuilder addCallerType2(Class<? extends CallerType> callerType2){
			this.callerTypes2.add(callerType2);
			return this;
		}

		public DatarouterAuthPlugin build(){
			return new DatarouterAuthPlugin(
					enableUserAuth,
					new DatarouterAuthDaoModule(
							defaultClientId,
							defaultClientId,
							defaultClientId,
							defaultClientId,
							defaultClientId,
							defaultClientId,
							defaultClientId,
							defaultClientId,
							defaultClientId,
							defaultClientId),
					userDeprovisioningStrategyClass,
					copyUserListenerClass,
					defaultDatarouterUserPassword,
					defaultApiKey,
					defaultSecretKey,
					datarouterAccountDeleteAction,
					callerTypes2,
					configs);
		}

	}

	public static class DatarouterAuthDaoModule extends DaosModuleBuilder{

		private final List<ClientId> datarouterAccountClientIds;
		private final List<ClientId> datarouterAccountCredentialClientIds;
		private final List<ClientId> datarouterAccountSecretCredentialClientIds;
		private final List<ClientId> datarouterAccountPermissionClientIds;
		private final List<ClientId> datarouterPermissionRequestClientIds;
		private final List<ClientId> datarouterSamlClientIds;
		private final List<ClientId> datarouterUserAccountMapClientIds;
		private final List<ClientId> datarouterUserClientIds;
		private final List<ClientId> datarouterUserHistoryClientIds;
		private final List<ClientId> deprovisionedUserClientIds;

		public DatarouterAuthDaoModule(
				List<ClientId> datarouterAccountClientIds,
				List<ClientId> datarouterAccountCredentialClientIds,
				List<ClientId> datarouterAccountSecretCredentialClientIds,
				List<ClientId> datarouterAccountPermissionClientIds,
				List<ClientId> datarouterPermissionRequestClientIds,
				List<ClientId> datarouterSamlClientIds,
				List<ClientId> datarouterUserAccountMapClientIds,
				List<ClientId> datarouterUserClientIds,
				List<ClientId> datarouterUserHistoryClientIds,
				List<ClientId> deprovisionedUserClientIds){
			this.datarouterAccountClientIds = datarouterAccountClientIds;
			this.datarouterAccountCredentialClientIds = datarouterAccountCredentialClientIds;
			this.datarouterAccountSecretCredentialClientIds = datarouterAccountSecretCredentialClientIds;
			this.datarouterAccountPermissionClientIds = datarouterAccountPermissionClientIds;
			this.datarouterPermissionRequestClientIds = datarouterPermissionRequestClientIds;
			this.datarouterSamlClientIds = datarouterSamlClientIds;
			this.datarouterUserAccountMapClientIds = datarouterUserAccountMapClientIds;
			this.datarouterUserClientIds = datarouterUserClientIds;
			this.datarouterUserHistoryClientIds = datarouterUserHistoryClientIds;
			this.deprovisionedUserClientIds = deprovisionedUserClientIds;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			return List.of(
					DatarouterAccountDao.class,
					DatarouterAccountCredentialDao.class,
					DatarouterAccountSecretCredentialDao.class,
					DatarouterAccountPermissionDao.class,
					DatarouterPermissionRequestDao.class,
					DatarouterUserAccountMapDao.class,
					DatarouterUserDao.class,
					DatarouterUserHistoryDao.class,
					DatarouterSamlDao.class,
					DeprovisionedUserDao.class);
		}

		@Override
		public void configure(){
			bind(DatarouterUserDaoParams.class)
					.toInstance(new DatarouterUserDaoParams(datarouterUserClientIds));
			bind(DatarouterUserHistoryDaoParams.class)
					.toInstance(new DatarouterUserHistoryDaoParams(datarouterUserHistoryClientIds));
			bind(DatarouterPermissionRequestDaoParams.class)
					.toInstance(new DatarouterPermissionRequestDaoParams(datarouterPermissionRequestClientIds));
			bind(DatarouterAccountDaoParams.class)
					.toInstance(new DatarouterAccountDaoParams(datarouterAccountClientIds));
			bind(DatarouterAccountCredentialDaoParams.class)
					.toInstance(new DatarouterAccountCredentialDaoParams(datarouterAccountCredentialClientIds));
			bind(DatarouterAccountSecretCredentialDaoParams.class)
					.toInstance(new DatarouterAccountSecretCredentialDaoParams(
					datarouterAccountSecretCredentialClientIds));
			bind(DatarouterAccountPermissionDaoParams.class)
					.toInstance(new DatarouterAccountPermissionDaoParams(datarouterAccountPermissionClientIds));
			bind(DatarouterUserAccountMapDaoParams.class)
					.toInstance(new DatarouterUserAccountMapDaoParams(datarouterUserAccountMapClientIds));
			bind(DatarouterSamlDaoParams.class)
					.toInstance(new DatarouterSamlDaoParams(datarouterSamlClientIds));
			bind(DeprovisionedUserDaoParams.class)
					.toInstance(new DeprovisionedUserDaoParams(deprovisionedUserClientIds));
		}

	}

}
