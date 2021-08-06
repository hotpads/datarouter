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
package io.datarouter.loggerconfig.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.loggerconfig.web.LoggingSettingsHandler;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.user.role.DatarouterUserRole;

@Singleton
public class DatarouterLoggingConfigRouteSet extends BaseRouteSet{

	@Inject
	public DatarouterLoggingConfigRouteSet(DatarouterLoggingConfigPaths paths){
		super(paths.datarouter.logging);
		handle(paths.datarouter.logging).withHandler(LoggingSettingsHandler.class);
		handle(paths.datarouter.logging.createLoggerConfig).withHandler(LoggingSettingsHandler.class);
		handle(paths.datarouter.logging.deleteAppender).withHandler(LoggingSettingsHandler.class);
		handle(paths.datarouter.logging.deleteLoggerConfig).withHandler(LoggingSettingsHandler.class);
		handle(paths.datarouter.logging.editConsoleAppender).withHandler(LoggingSettingsHandler.class);
		handle(paths.datarouter.logging.editFileAppender).withHandler(LoggingSettingsHandler.class);
		handle(paths.datarouter.logging.showForm).withHandler(LoggingSettingsHandler.class);
		handle(paths.datarouter.logging.testLog).withHandler(LoggingSettingsHandler.class);
		handle(paths.datarouter.logging.updateLoggerConfig).withHandler(LoggingSettingsHandler.class);
	}

	@Override
	protected DispatchRule applyDefault(DispatchRule rule){
		return rule
				.allowRoles(
						DatarouterUserRole.DATAROUTER_ADMIN,
						DatarouterUserRole.DATAROUTER_SETTINGS,
						DatarouterUserRole.DATAROUTER_MONITORING)
				.withIsSystemDispatchRule(true);
	}

}
