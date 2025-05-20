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
package io.datarouter.nodewatch.config;

import io.datarouter.auth.role.DatarouterUserRoleRegistry;
import io.datarouter.nodewatch.web.handler.NodewatchConfigsHandler;
import io.datarouter.nodewatch.web.handler.NodewatchMetadataMigrateHandler;
import io.datarouter.nodewatch.web.handler.NodewatchNodeNameHandler;
import io.datarouter.nodewatch.web.handler.NodewatchSlowSpansHandler;
import io.datarouter.nodewatch.web.handler.NodewatchSummaryHandler;
import io.datarouter.nodewatch.web.handler.NodewatchTableActionsHandler;
import io.datarouter.nodewatch.web.handler.NodewatchTableHandler;
import io.datarouter.nodewatch.web.handler.NodewatchTableStorageHandler;
import io.datarouter.nodewatch.web.handler.NodewatchTablesHandler;
import io.datarouter.nodewatch.web.handler.NodewatchThresholdEditHandler;
import io.datarouter.storage.tag.Tag;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.encoder.DatarouterDefaultHandlerCodec;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterNodewatchRouteSet extends BaseRouteSet{

	@Inject
	public DatarouterNodewatchRouteSet(){

		registerWithMonitoringRole(NodewatchTablesHandler.class);
		registerWithMonitoringRole(NodewatchSummaryHandler.class);
		registerWithMonitoringRole(NodewatchConfigsHandler.class);
		registerWithAdminRole(NodewatchMetadataMigrateHandler.class);

		// table
		registerWithMonitoringRole(NodewatchTableHandler.class);
		registerWithMonitoringRole(NodewatchTableStorageHandler.class);
		registerWithMonitoringRole(NodewatchNodeNameHandler.class);

		// table actions
		registerWithAdminRole(NodewatchTableActionsHandler.class);

		// slowSpans
		registerWithMonitoringRole(NodewatchSlowSpansHandler.class);

		// threshold
		registerWithAdminRole(NodewatchThresholdEditHandler.class);
	}

	@Override
	protected DispatchRule applyDefault(DispatchRule rule){
		return rule
				.allowRoles(DatarouterUserRoleRegistry.DATAROUTER_ADMIN)
				.withDefaultHandlerCodec(DatarouterDefaultHandlerCodec.INSTANCE)
				.withTag(Tag.DATAROUTER);
	}

	private void registerWithMonitoringRole(Class<? extends BaseHandler> handlerClass){
		registerHandler(handlerClass)
				.values()
				.forEach(rule -> rule.allowRoles(DatarouterUserRoleRegistry.DATAROUTER_MONITORING));
	}

	private void registerWithAdminRole(Class<? extends BaseHandler> handlerClass){
		registerHandler(handlerClass)
				.values()
				.forEach(rule -> rule.allowRoles(DatarouterUserRoleRegistry.DATAROUTER_ADMIN));
	}
}
