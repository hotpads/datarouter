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
package io.datarouter.auth.config;

import java.util.List;

import com.google.inject.name.Names;

import io.datarouter.auth.service.DatarouterUserDeprovisioningService;
import io.datarouter.auth.service.DatarouterUserInfo;
import io.datarouter.auth.service.DefaultDatarouterAccountKeys;
import io.datarouter.auth.service.DefaultDatarouterAccountKeysSupplier;
import io.datarouter.auth.service.DefaultDatarouterUserPassword;
import io.datarouter.auth.service.DefaultDatarouterUserPasswordSupplier;
import io.datarouter.auth.service.UserDeprovisioningService;
import io.datarouter.auth.service.UserDeprovisioningService.ShouldFlagUsersInsteadOfDeprovisioningSupplier;
import io.datarouter.auth.service.UserInfo;
import io.datarouter.auth.storage.account.BaseDatarouterAccountDao;
import io.datarouter.auth.storage.account.DatarouterAccountDao;
import io.datarouter.auth.storage.account.DatarouterAccountDao.DatarouterAccountDaoParams;
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
import io.datarouter.instrumentation.changelog.ChangelogPublisher;
import io.datarouter.instrumentation.changelog.ChangelogPublisher.NoOpChangelogPublisher;
import io.datarouter.job.config.BaseJobPlugin;
import io.datarouter.job.config.DatarouterJobRouteSet;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.web.navigation.AppNavBarCategory;
import io.datarouter.web.user.BaseDatarouterSessionDao;
import io.datarouter.web.user.DatarouterSessionDao;
import io.datarouter.web.user.authenticate.saml.BaseDatarouterSamlDao;
import io.datarouter.web.user.authenticate.saml.DatarouterSamlDao;
import io.datarouter.web.user.authenticate.saml.DatarouterSamlDao.DatarouterSamlDaoParams;

public class DatarouterAuthPlugin extends BaseJobPlugin{

	public static final String NAMED_Changelog = "DatarouterAuthChangeLog";

	private static final DatarouterAuthPaths PATHS = new DatarouterAuthPaths();

	private final Class<? extends UserInfo> userInfoClass;
	private final boolean shouldMarkUsersInsteadOfDeprovisioning;
	private final Class<? extends UserDeprovisioningService> userDeprovisioningServiceClass;
	private final String defaultDatarouterUserPassword;
	private final String defaultApiKey;
	private final String defaultSecretKey;
	private final Class<? extends ChangelogPublisher> changelogPublisher;

	private DatarouterAuthPlugin(
			boolean enableUserAuth,
			DatarouterAuthDaoModule daosModuleBuilder,
			Class<? extends UserInfo> userInfoClass,
			boolean shouldMarkUsersInsteadOfDeprovisioning,
			Class<? extends UserDeprovisioningService> userDeprovisioningServiceClass,
			String defaultDatarouterUserPassword,
			String defaultApiKey,
			String defaultSecretKey,
			Class<? extends ChangelogPublisher> changelogPublisher){
		this.userInfoClass = userInfoClass;
		this.shouldMarkUsersInsteadOfDeprovisioning = shouldMarkUsersInsteadOfDeprovisioning;
		this.userDeprovisioningServiceClass = userDeprovisioningServiceClass;
		this.defaultDatarouterUserPassword = defaultDatarouterUserPassword;
		this.defaultApiKey = defaultApiKey;
		this.defaultSecretKey = defaultSecretKey;
		this.changelogPublisher = changelogPublisher;

		if(enableUserAuth){
			addAppListener(DatarouterUserConfigAppListener.class);
			addAppNavBarItem(AppNavBarCategory.ADMIN, PATHS.admin.viewUsers, "View Users");
			addAppNavBarItem(AppNavBarCategory.USER, PATHS.admin.editUser, "Edit User");
			addAppNavBarItem(AppNavBarCategory.USER, PATHS.permissionRequest, "Permission Request");
			addAppNavBarItem(AppNavBarCategory.USER, PATHS.resetPassword, "Reset Password");
			addAppNavBarItem(AppNavBarCategory.USER, PATHS.admin.createUser, "Create User");
			addRouteSetOrdered(DatarouterAuthRouteSet.class, DatarouterJobRouteSet.class);
		}

		addAppListener(DatarouterAccountConfigAppListener.class);
		addAppNavBarItem(AppNavBarCategory.ADMIN, PATHS.admin.accounts, "Account Manager");
		addAppNavBarItem(AppNavBarCategory.ADMIN, PATHS.userDeprovisioning, "User Deprovisioning");
		addAppNavBarItem(AppNavBarCategory.DOCS, PATHS.docs.toSlashedStringWithTrailingSlash(), "Docs");
		addRouteSet(DatarouterAccountRouteSet.class);
		addRouteSet(DatarouterDocumentationRouteSet.class);
		addRouteSet(UserDeprovisioningRouteSet.class);
		addSettingRoot(DatarouterAuthSettingRoot.class);
		addTriggerGroup(DatarouterAuthTriggerGroup.class);
		setDaosModule(daosModuleBuilder);
	}

	@Override
	public String getName(){
		return "DatarouterAuth";
	}

	@Override
	protected void configure(){
		bindActual(BaseDatarouterSessionDao.class, DatarouterSessionDao.class);
		bindActual(BaseDatarouterAccountDao.class, DatarouterAccountDao.class);
		bindActual(BaseDatarouterAccountPermissionDao.class, DatarouterAccountPermissionDao.class);
		bindActual(BaseDatarouterUserAccountMapDao.class, DatarouterUserAccountMapDao.class);
		bindActual(BaseDatarouterSamlDao.class, DatarouterSamlDao.class);
		bind(UserInfo.class).to(userInfoClass);
		bind(ShouldFlagUsersInsteadOfDeprovisioningSupplier.class).toInstance(
				new ShouldFlagUsersInsteadOfDeprovisioningSupplier(shouldMarkUsersInsteadOfDeprovisioning));
		bind(UserDeprovisioningService.class).to(userDeprovisioningServiceClass);
		bindActualInstance(DefaultDatarouterUserPasswordSupplier.class,
				new DefaultDatarouterUserPassword(defaultDatarouterUserPassword));
		bindActualInstance(DefaultDatarouterAccountKeysSupplier.class,
				new DefaultDatarouterAccountKeys(defaultApiKey, defaultSecretKey));
		bind(ChangelogPublisher.class)
				.annotatedWith(Names.named(NAMED_Changelog))
				.to(changelogPublisher);
	}

	public static class DatarouterAuthPluginBuilder{

		private final boolean enableUserAuth;
		private final ClientId defaultClientId;

		private DatarouterAuthDaoModule daoModule;
		private Class<? extends UserInfo> userInfoClass = DatarouterUserInfo.class;
		private Class<? extends UserDeprovisioningService> userDeprovisioningServiceClass =
				DatarouterUserDeprovisioningService.class;
		private String defaultDatarouterUserPassword = "";
		private String defaultApiKey = "";
		private String defaultSecretKey = "";
		private boolean shouldMarkUsersInsteadOfDeprovisioning = false;
		private Class<? extends ChangelogPublisher> changelogPublisher = NoOpChangelogPublisher.class;

		public DatarouterAuthPluginBuilder(boolean enableUserAuth, ClientId defaultClientId,
				String defaultDatarouterUserPassword, String defaultApiKey, String defaultSecretKey){
			this.enableUserAuth = enableUserAuth;
			this.defaultClientId = defaultClientId;
			this.defaultDatarouterUserPassword = defaultDatarouterUserPassword;
			this.defaultApiKey = defaultApiKey;
			this.defaultSecretKey = defaultSecretKey;
		}

		public DatarouterAuthPluginBuilder setDaoModule(DatarouterAuthDaoModule daoModule){
			this.daoModule = daoModule;
			return this;
		}

		public DatarouterAuthPluginBuilder setUserInfoClass(Class<? extends UserInfo> userInfoClass){
			this.userInfoClass = userInfoClass;
			return this;
		}

		public DatarouterAuthPluginBuilder setUserDeprovisioningServiceClass(
				Class<? extends UserDeprovisioningService> userDeprovisioningServiceClass){
			this.userDeprovisioningServiceClass = userDeprovisioningServiceClass;
			return this;
		}

		public DatarouterAuthPluginBuilder enableMarkingUsersInsteadOfDeprovisioning(){
			this.shouldMarkUsersInsteadOfDeprovisioning = true;
			return this;
		}

		public DatarouterAuthPluginBuilder enableChangelogPublishing(
				Class<? extends ChangelogPublisher> changelogPublisher){
			this.changelogPublisher = changelogPublisher;
			return this;
		}

		public DatarouterAuthPlugin build(){
			return new DatarouterAuthPlugin(
					enableUserAuth,
					daoModule == null
							? new DatarouterAuthDaoModule(
									defaultClientId,
									defaultClientId,
									defaultClientId,
									defaultClientId,
									defaultClientId,
									defaultClientId,
									defaultClientId,
									defaultClientId)
							: daoModule,
					userInfoClass,
					shouldMarkUsersInsteadOfDeprovisioning,
					userDeprovisioningServiceClass,
					defaultDatarouterUserPassword,
					defaultApiKey,
					defaultSecretKey,
					changelogPublisher);
		}

	}

	public static class DatarouterAuthDaoModule extends DaosModuleBuilder{

		private final ClientId datarouterAccountClientId;
		private final ClientId datarouterAccountPermissionClientId;
		private final ClientId datarouterPermissionRequestClientId;
		private final ClientId datarouterSamlClientId;
		private final ClientId datarouterUserAccountMapClientId;
		private final ClientId datarouterUserClientId;
		private final ClientId datarouterUserHistoryClientId;
		private final ClientId deprovisionedUserClientId;

		public DatarouterAuthDaoModule(
				ClientId datarouterAccountClientId,
				ClientId datarouterAccountPermissionClientId,
				ClientId datarouterPermissionRequestClientId,
				ClientId datarouterSamlClientId,
				ClientId datarouterUserAccountMapClientId,
				ClientId datarouterUserClientId,
				ClientId datarouterUserHistoryClientId,
				ClientId deprovisionedUserClientId){
			this.datarouterAccountClientId = datarouterAccountClientId;
			this.datarouterAccountPermissionClientId = datarouterAccountPermissionClientId;
			this.datarouterPermissionRequestClientId = datarouterPermissionRequestClientId;
			this.datarouterSamlClientId = datarouterSamlClientId;
			this.datarouterUserAccountMapClientId = datarouterUserAccountMapClientId;
			this.datarouterUserClientId = datarouterUserClientId;
			this.datarouterUserHistoryClientId = datarouterUserHistoryClientId;
			this.deprovisionedUserClientId = deprovisionedUserClientId;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			return List.of(
					DatarouterAccountDao.class,
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
					.toInstance(new DatarouterUserDaoParams(datarouterUserClientId));
			bind(DatarouterUserHistoryDaoParams.class)
					.toInstance(new DatarouterUserHistoryDaoParams(datarouterUserHistoryClientId));
			bind(DatarouterPermissionRequestDaoParams.class)
					.toInstance(new DatarouterPermissionRequestDaoParams(datarouterPermissionRequestClientId));
			bind(DatarouterAccountDaoParams.class)
					.toInstance(new DatarouterAccountDaoParams(datarouterAccountClientId));
			bind(DatarouterAccountPermissionDaoParams.class)
					.toInstance(new DatarouterAccountPermissionDaoParams(datarouterAccountPermissionClientId));
			bind(DatarouterUserAccountMapDaoParams.class)
					.toInstance(new DatarouterUserAccountMapDaoParams(datarouterUserAccountMapClientId));
			bind(DatarouterSamlDaoParams.class)
					.toInstance(new DatarouterSamlDaoParams(datarouterSamlClientId));
			bind(DeprovisionedUserDaoParams.class)
					.toInstance(new DeprovisionedUserDaoParams(deprovisionedUserClientId));
		}

	}

}
