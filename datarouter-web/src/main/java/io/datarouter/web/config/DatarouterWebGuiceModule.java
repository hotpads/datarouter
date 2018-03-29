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

import com.google.inject.multibindings.OptionalBinder;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;

import io.datarouter.httpclient.json.GsonJsonSerializer;
import io.datarouter.httpclient.json.JsonSerializer;
import io.datarouter.storage.config.guice.DatarouterStorageGuiceModule;
import io.datarouter.storage.setting.MemorySettingFinder;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.web.config.guice.DatarouterWebExecutorGuiceModule;
import io.datarouter.web.exception.ExceptionRecorder;
import io.datarouter.web.handler.encoder.HandlerEncoder;
import io.datarouter.web.handler.mav.DatarouterMavPropertiesFactoryConfig;
import io.datarouter.web.handler.mav.MavPropertiesFactoryConfig;
import io.datarouter.web.handler.mav.nav.NavBar;
import io.datarouter.web.port.CompoundPortIdentifier;
import io.datarouter.web.port.PortIdentifier;
import io.datarouter.web.user.DatarouterUserNodes;
import io.datarouter.web.user.NoOpDatarouterUserNodes;
import io.datarouter.web.user.authenticate.config.BaseDatarouterAuthenticationConfig;
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;
import io.datarouter.web.user.authenticate.saml.SamlRegistrar;
import io.datarouter.web.user.session.service.DatarouterRoleManager;
import io.datarouter.web.user.session.service.DatarouterUserSessionService;
import io.datarouter.web.user.session.service.RoleManager;
import io.datarouter.web.user.session.service.UserSessionService;

public class DatarouterWebGuiceModule extends ServletModule{

	@Override
	protected void configureServlets(){
		install(new DatarouterStorageGuiceModule());
		install(new DatarouterWebExecutorGuiceModule());

		bind(ServletContextProvider.class).toInstance(new ServletContextProvider(getServletContext()));
		bind(JsonSerializer.class).annotatedWith(Names.named(HandlerEncoder.DEFAULT_HANDLER_SERIALIZER)).to(
				GsonJsonSerializer.class);
		bind(PortIdentifier.class).annotatedWith(Names.named(CompoundPortIdentifier.COMPOUND_PORT_IDENTIFIER))
				.to(CompoundPortIdentifier.class);

		bindOptional(DatarouterAuthenticationConfig.class).setDefault().to(BaseDatarouterAuthenticationConfig.class);
		bindOptional(DatarouterUserNodes.class).setDefault().to(NoOpDatarouterUserNodes.class);
		bindOptional(ExceptionRecorder.class);
		bindOptional(MavPropertiesFactoryConfig.class).setDefault().to(DatarouterMavPropertiesFactoryConfig.class);
		bindOptional(NavBar.class);
		bindOptional(RoleManager.class).setDefault().to(DatarouterRoleManager.class);
		bindOptional(SamlRegistrar.class);
		bindOptional(SettingFinder.class).setDefault().to(MemorySettingFinder.class);
		bindOptional(UserSessionService.class).setDefault().to(DatarouterUserSessionService.class);
	}

	private <T> OptionalBinder<T> bindOptional(Class<T> type){
		return OptionalBinder.newOptionalBinder(binder(), type);
	}

}
