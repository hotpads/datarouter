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

import io.datarouter.auth.role.DatarouterUserRole;
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
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterNodewatchRouteSet extends BaseRouteSet{

	@Inject
	public DatarouterNodewatchRouteSet(DatarouterNodewatchPaths paths){

		handle(paths.datarouter.nodewatch.tables)
				.withHandler(NodewatchTablesHandler.class)
				.allowRoles(DatarouterUserRole.DATAROUTER_MONITORING);

		handle(paths.datarouter.nodewatch.summary)
				.withHandler(NodewatchSummaryHandler.class)
				.allowRoles(DatarouterUserRole.DATAROUTER_MONITORING);

		handle(paths.datarouter.nodewatch.configs)
				.withHandler(NodewatchConfigsHandler.class)
				.allowRoles(DatarouterUserRole.DATAROUTER_MONITORING);

		handle(paths.datarouter.nodewatch.metadata.migrate)
				.withHandler(NodewatchMetadataMigrateHandler.class)
				.allowRoles(DatarouterUserRole.DATAROUTER_ADMIN);

		// table
		handle(paths.datarouter.nodewatch.table)
				.withHandler(NodewatchTableHandler.class)
				.allowRoles(DatarouterUserRole.DATAROUTER_MONITORING);
		handle(paths.datarouter.nodewatch.table.storage)
				.withHandler(NodewatchTableStorageHandler.class)
				.allowRoles(DatarouterUserRole.DATAROUTER_MONITORING);
		handle(paths.datarouter.nodewatch.table.nodeName)
				.withHandler(NodewatchNodeNameHandler.class)
				.allowRoles(DatarouterUserRole.DATAROUTER_MONITORING);

		// table actions
		handle(paths.datarouter.nodewatch.table.resample)
				.withHandler(NodewatchTableActionsHandler.class)
				.allowRoles(DatarouterUserRole.DATAROUTER_ADMIN);
		handle(paths.datarouter.nodewatch.table.deleteSamples)
				.withHandler(NodewatchTableActionsHandler.class)
				.allowRoles(DatarouterUserRole.DATAROUTER_ADMIN);
		handle(paths.datarouter.nodewatch.table.deleteAllMetadata)
				.withHandler(NodewatchTableActionsHandler.class)
				.allowRoles(DatarouterUserRole.DATAROUTER_ADMIN);

		// slowSpans
		handle(paths.datarouter.nodewatch.slowSpans)
				.withHandler(NodewatchSlowSpansHandler.class)
				.allowRoles(DatarouterUserRole.DATAROUTER_MONITORING);

		// threshold
		handle(paths.datarouter.nodewatch.threshold.edit)
				.withHandler(NodewatchThresholdEditHandler.class)
				.allowRoles(DatarouterUserRole.DATAROUTER_ADMIN);
		handle(paths.datarouter.nodewatch.threshold.delete)
				.withHandler(NodewatchThresholdEditHandler.class)
				.allowRoles(DatarouterUserRole.DATAROUTER_ADMIN);
	}

	@Override
	protected DispatchRule applyDefault(DispatchRule rule){
		return rule
				.allowRoles(DatarouterUserRole.DATAROUTER_ADMIN)
				.withTag(Tag.DATAROUTER);
	}

}
