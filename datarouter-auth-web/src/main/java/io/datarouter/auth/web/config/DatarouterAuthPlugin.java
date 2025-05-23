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
package io.datarouter.auth.web.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.datarouter.auth.config.DatarouterAuthPaths;
import io.datarouter.auth.service.DatarouterUserService;
import io.datarouter.auth.service.PermissionRequestUserInfo;
import io.datarouter.auth.service.UserInfo;
import io.datarouter.auth.service.deprovisioning.DatarouterUserDeprovisioningStrategy;
import io.datarouter.auth.service.deprovisioning.UserDeprovisioningStrategy;
import io.datarouter.auth.storage.account.DatarouterAccountDao;
import io.datarouter.auth.storage.account.DatarouterAccountDao.DatarouterAccountDaoParams;
import io.datarouter.auth.storage.account.credential.DatarouterAccountCredentialDao;
import io.datarouter.auth.storage.account.credential.DatarouterAccountCredentialDao.DatarouterAccountCredentialDaoParams;
import io.datarouter.auth.storage.account.credential.secret.DatarouterAccountSecretCredentialDao;
import io.datarouter.auth.storage.account.credential.secret.DatarouterAccountSecretCredentialDao.DatarouterAccountSecretCredentialDaoParams;
import io.datarouter.auth.storage.account.permission.DatarouterAccountPermissionDao;
import io.datarouter.auth.storage.account.permission.DatarouterAccountPermissionDao.DatarouterAccountPermissionDaoParams;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUserDao;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUserDao.DatarouterUserDaoParams;
import io.datarouter.auth.storage.user.datarouteruser.cache.DatarouterUserByIdCache;
import io.datarouter.auth.storage.user.datarouteruser.cache.DatarouterUserByUserTokenCache;
import io.datarouter.auth.storage.user.datarouteruser.cache.DatarouterUserByUsernameCache;
import io.datarouter.auth.storage.user.permissionrequest.DatarouterPermissionRequestDao;
import io.datarouter.auth.storage.user.permissionrequest.DatarouterPermissionRequestDao.DatarouterPermissionRequestDaoParams;
import io.datarouter.auth.storage.user.roleapprovals.DatarouterUserRoleApprovalDao;
import io.datarouter.auth.storage.user.roleapprovals.DatarouterUserRoleApprovalDao.DatarouterUserRoleApprovalDaoParams;
import io.datarouter.auth.storage.user.saml.BaseDatarouterSamlDao;
import io.datarouter.auth.storage.user.saml.DatarouterSamlDao;
import io.datarouter.auth.storage.user.saml.DatarouterSamlDao.DatarouterSamlDaoParams;
import io.datarouter.auth.storage.user.session.BaseDatarouterSessionDao;
import io.datarouter.auth.storage.user.session.DatarouterSessionDao;
import io.datarouter.auth.storage.user.useraccountmap.BaseDatarouterUserAccountMapDao;
import io.datarouter.auth.storage.user.useraccountmap.DatarouterUserAccountMapDao;
import io.datarouter.auth.storage.user.useraccountmap.DatarouterUserAccountMapDao.DatarouterUserAccountMapDaoParams;
import io.datarouter.auth.storage.user.userhistory.DatarouterUserHistoryDao;
import io.datarouter.auth.storage.user.userhistory.DatarouterUserHistoryDao.DatarouterUserHistoryDaoParams;
import io.datarouter.auth.web.config.routeset.DatarouterAccountApiRouteSet;
import io.datarouter.auth.web.config.routeset.DatarouterAccountRouteSet;
import io.datarouter.auth.web.config.routeset.DatarouterAuthRouteSet;
import io.datarouter.auth.web.config.routeset.DatarouterDocumentationRouteSet;
import io.datarouter.auth.web.config.routeset.DatarouterRoleRequirementsRouteSet;
import io.datarouter.auth.web.service.AccountCallerTypeRegistry2;
import io.datarouter.auth.web.service.DatarouterAccountDailyDigest;
import io.datarouter.auth.web.service.DatarouterDefaultAccountKeysDailyDigest;
import io.datarouter.auth.web.service.DatarouterDefaultStaleAccountsDailyDigest;
import io.datarouter.auth.web.service.DatarouterDefaultUserDailyDigest;
import io.datarouter.auth.web.service.DatarouterPermissionRequestUserInfo;
import io.datarouter.auth.web.service.DefaultDatarouterAccountKeys;
import io.datarouter.auth.web.service.DefaultDatarouterAccountKeysSupplier;
import io.datarouter.auth.web.service.DefaultDatarouterUserPassword;
import io.datarouter.auth.web.service.DefaultDatarouterUserPasswordSupplier;
import io.datarouter.auth.web.service.PermissionRequestDailyDigest;
import io.datarouter.autoconfig.service.AutoConfigGroup;
import io.datarouter.httpclient.endpoint.caller.CallerType;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.job.config.DatarouterJobRouteSet;
import io.datarouter.plugin.PluginConfigKey;
import io.datarouter.plugin.PluginConfigValue;
import io.datarouter.storage.cache.Cache;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.navigation.AppNavBarCategory;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterAuthPlugin extends BaseWebPlugin{

	private static final DatarouterAuthPaths PATHS = new DatarouterAuthPaths();

	private final Class<? extends UserDeprovisioningStrategy> userDeprovisioningStrategyClass;
	private final String defaultDatarouterUserPassword;
	private final String defaultApiKey;
	private final String defaultSecretKey;
	private final List<Class<? extends CallerType>> callerTypes2;

	private DatarouterAuthPlugin(
			boolean enableUserAuth,
			DatarouterAuthDaoModule daosModuleBuilder,
			Class<? extends UserDeprovisioningStrategy> userDeprovisioningStrategyClass,
			String defaultDatarouterUserPassword,
			String defaultApiKey,
			String defaultSecretKey,
			List<Class<? extends CallerType>> callerTypes2,
			Map<PluginConfigKey<?>,Class<? extends PluginConfigValue<?>>> configs){
		this.userDeprovisioningStrategyClass = userDeprovisioningStrategyClass;
		this.defaultDatarouterUserPassword = defaultDatarouterUserPassword;
		this.defaultApiKey = defaultApiKey;
		this.defaultSecretKey = defaultSecretKey;
		this.callerTypes2 = callerTypes2;

		if(enableUserAuth){
			addAppListener(DatarouterUserConfigAppListener.class);
			addAppNavBarItem(AppNavBarCategory.USER, PATHS.admin.viewUsers, "View Users");
			addAppNavBarItem(AppNavBarCategory.USER, PATHS.admin.editUser, "Edit User");
			addAppNavBarItem(AppNavBarCategory.USER, PATHS.permissionRequest, "Permission Request");
			addDynamicNavBarItem(CreateUserNavBarItem.class);
			addRouteSetOrdered(DatarouterAuthRouteSet.class, DatarouterJobRouteSet.class);
		}

		addAppListener(DatarouterAccountConfigAppListener.class);
		addDatarouterNavBarItem(
				DatarouterNavBarCategory.CONFIGURATION,
				PATHS.datarouter.accountManager,
				"Accounts");

		addDynamicNavBarItem(ApiDocsNavBarItem.class);
		addDynamicNavBarItem(ApiDocsSchemaNavBarItem.class);
		addRouteSet(DatarouterAccountApiRouteSet.class);
		addRouteSet(DatarouterAccountRouteSet.class);
		addRouteSet(DatarouterDocumentationRouteSet.class);
		addRouteSet(DatarouterRoleRequirementsRouteSet.class);
		addSettingRoot(DatarouterAuthSettingRoot.class);
		addPluginEntry(AutoConfigGroup.KEY, DatarouterDefaultAccountAutoConfig.class);
		addPluginEntry(BaseTriggerGroup.KEY, DatarouterAuthTriggerGroup.class);
		addPluginEntry(Cache.KEY, DatarouterUserByIdCache.class);
		addPluginEntry(Cache.KEY, DatarouterUserByUsernameCache.class);
		addPluginEntry(Cache.KEY, DatarouterUserByUserTokenCache.class);
		setDaosModule(daosModuleBuilder);
		addDatarouterGithubDocLink("datarouter-auth-web");
		addDailyDigest(PermissionRequestDailyDigest.class);
		addDailyDigest(DatarouterAccountDailyDigest.class);
		addDailyDigest(DatarouterDefaultAccountKeysDailyDigest.class);
		addDailyDigest(DatarouterDefaultStaleAccountsDailyDigest.class);
		addDailyDigest(DatarouterDefaultUserDailyDigest.class);
		configs.forEach(this::addPluginEntry);
	}

	@Override
	protected void configure(){
		bindActual(BaseDatarouterSessionDao.class, DatarouterSessionDao.class);
		bindActual(BaseDatarouterUserAccountMapDao.class, DatarouterUserAccountMapDao.class);
		bindActual(BaseDatarouterSamlDao.class, DatarouterSamlDao.class);

		bind(UserDeprovisioningStrategy.class).to(userDeprovisioningStrategyClass);
		bindActualInstance(DefaultDatarouterUserPasswordSupplier.class,
				new DefaultDatarouterUserPassword(defaultDatarouterUserPassword));
		bindActualInstance(DefaultDatarouterAccountKeysSupplier.class,
				new DefaultDatarouterAccountKeys(defaultApiKey, defaultSecretKey));
		bindActualInstance(AccountCallerTypeRegistry2.class, new AccountCallerTypeRegistry2(callerTypes2));
	}

	public static class DatarouterAuthPluginBuilder{

		private final boolean enableUserAuth;
		private final List<ClientId> defaultClientId;

		private Map<PluginConfigKey<?>,Class<? extends PluginConfigValue<?>>> configs = new HashMap<>();

		private Class<? extends UserDeprovisioningStrategy> userDeprovisioningStrategyClass =
				DatarouterUserDeprovisioningStrategy.class;
		private String defaultDatarouterUserPassword = "";
		private String defaultApiKey = "";
		private String defaultSecretKey = "";
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
			configs.put(UserInfo.KEY, DatarouterUserService.class);
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
					defaultDatarouterUserPassword,
					defaultApiKey,
					defaultSecretKey,
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
		private final List<ClientId> datarouterUserRoleApprovalClientIds;

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
				List<ClientId> datarouterUserRoleApprovalClientIds){
			this.datarouterAccountClientIds = datarouterAccountClientIds;
			this.datarouterAccountCredentialClientIds = datarouterAccountCredentialClientIds;
			this.datarouterAccountSecretCredentialClientIds = datarouterAccountSecretCredentialClientIds;
			this.datarouterAccountPermissionClientIds = datarouterAccountPermissionClientIds;
			this.datarouterPermissionRequestClientIds = datarouterPermissionRequestClientIds;
			this.datarouterSamlClientIds = datarouterSamlClientIds;
			this.datarouterUserAccountMapClientIds = datarouterUserAccountMapClientIds;
			this.datarouterUserClientIds = datarouterUserClientIds;
			this.datarouterUserHistoryClientIds = datarouterUserHistoryClientIds;
			this.datarouterUserRoleApprovalClientIds = datarouterUserRoleApprovalClientIds;
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
					DatarouterUserRoleApprovalDao.class);
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
			bind(DatarouterUserRoleApprovalDaoParams.class)
					.toInstance(new DatarouterUserRoleApprovalDaoParams(datarouterUserRoleApprovalClientIds));
		}

	}

}
