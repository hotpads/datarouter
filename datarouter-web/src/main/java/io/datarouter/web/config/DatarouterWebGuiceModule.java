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
package io.datarouter.web.config;

import java.util.Collections;

import com.google.gson.Gson;
import com.google.inject.name.Names;

import io.datarouter.httpclient.json.GsonJsonSerializer;
import io.datarouter.httpclient.json.JsonSerializer;
import io.datarouter.storage.config.DatarouterAdditionalAdministrators;
import io.datarouter.storage.config.guice.DatarouterStorageGuiceModule;
import io.datarouter.storage.setting.MemorySettingFinder;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.util.serialization.GsonTool;
import io.datarouter.web.exception.ExceptionRecorder;
import io.datarouter.web.handler.encoder.HandlerEncoder;
import io.datarouter.web.handler.mav.DatarouterMavPropertiesFactoryConfig;
import io.datarouter.web.handler.mav.MavPropertiesFactoryConfig;
import io.datarouter.web.handler.mav.nav.AppNavBar;
import io.datarouter.web.inject.guice.BaseGuiceServletModule;
import io.datarouter.web.monitoring.latency.LatencyMonitoringGraphLink;
import io.datarouter.web.monitoring.latency.LatencyMonitoringGraphLink.NoOpLatencyMonitoringGraphLink;
import io.datarouter.web.port.CompoundPortIdentifier;
import io.datarouter.web.port.PortIdentifier;
import io.datarouter.web.user.BaseDatarouterPermissionRequestDao;
import io.datarouter.web.user.BaseDatarouterPermissionRequestDao.NoOpDatarouterPermissionRequestDao;
import io.datarouter.web.user.BaseDatarouterSessionDao;
import io.datarouter.web.user.BaseDatarouterSessionDao.NoOpDatarouterSessionDao;
import io.datarouter.web.user.BaseDatarouterUserDao;
import io.datarouter.web.user.BaseDatarouterUserDao.NoOpDatarouterUserDao;
import io.datarouter.web.user.BaseDatarouterUserHistoryDao;
import io.datarouter.web.user.BaseDatarouterUserHistoryDao.NoOpDatarouterUserHistoryDao;
import io.datarouter.web.user.authenticate.PermissionRequestAdditionalEmails;
import io.datarouter.web.user.authenticate.config.BaseDatarouterAuthenticationConfig;
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;
import io.datarouter.web.user.authenticate.saml.SamlRegistrar;
import io.datarouter.web.user.session.CurrentUserSessionInfo;
import io.datarouter.web.user.session.DatarouterCurrentUserSessionInfo;
import io.datarouter.web.user.session.service.DatarouterRoleManager;
import io.datarouter.web.user.session.service.DatarouterUserInfo;
import io.datarouter.web.user.session.service.DatarouterUserSessionService;
import io.datarouter.web.user.session.service.RoleManager;
import io.datarouter.web.user.session.service.UserInfo;
import io.datarouter.web.user.session.service.UserSessionService;

public class DatarouterWebGuiceModule extends BaseGuiceServletModule{

	@Override
	protected void configureServlets(){
		install(new DatarouterStorageGuiceModule());

		bind(ServletContextProvider.class).toInstance(new ServletContextProvider(getServletContext()));
		bind(JsonSerializer.class)
				.annotatedWith(Names.named(HandlerEncoder.DEFAULT_HANDLER_SERIALIZER))
				.to(GsonJsonSerializer.class);
		bind(PortIdentifier.class)
				.annotatedWith(Names.named(CompoundPortIdentifier.COMPOUND_PORT_IDENTIFIER))
				.to(CompoundPortIdentifier.class);

		bindDefault(DatarouterAuthenticationConfig.class, BaseDatarouterAuthenticationConfig.class);
		bindDefault(BaseDatarouterUserDao.class, NoOpDatarouterUserDao.class);
		bindDefault(BaseDatarouterUserHistoryDao.class, NoOpDatarouterUserHistoryDao.class);
		bindDefault(BaseDatarouterPermissionRequestDao.class, NoOpDatarouterPermissionRequestDao.class);
		bindDefault(BaseDatarouterSessionDao.class, NoOpDatarouterSessionDao.class);
		bindDefault(CurrentUserSessionInfo.class, DatarouterCurrentUserSessionInfo.class);
		bindDefault(UserInfo.class, DatarouterUserInfo.class);
		optionalBinder(ExceptionRecorder.class);
		bindDefault(MavPropertiesFactoryConfig.class, DatarouterMavPropertiesFactoryConfig.class);
		optionalBinder(AppNavBar.class);
		bindDefault(RoleManager.class, DatarouterRoleManager.class);
		optionalBinder(SamlRegistrar.class);
		bindDefault(SettingFinder.class, MemorySettingFinder.class);
		bindDefault(UserSessionService.class, DatarouterUserSessionService.class);
		bindDefaultInstance(DatarouterAdditionalAdministrators.class,
				new DatarouterAdditionalAdministrators(Collections.emptySet()));
		bindDefault(LatencyMonitoringGraphLink.class, NoOpLatencyMonitoringGraphLink.class);
		bindDefaultInstance(PermissionRequestAdditionalEmails.class,
				new PermissionRequestAdditionalEmails(Collections.emptySet()));

		// define as singleton for everybody
		bind(Gson.class).toInstance(GsonTool.GSON);
	}

}
