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
package io.datarouter.web.dispatcher;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.web.browse.DatabeanViewerHandler;
import io.datarouter.web.browse.DatarouterClientHandler;
import io.datarouter.web.browse.DeleteNodeDataHandler;
import io.datarouter.web.browse.GetNodeDataHandler;
import io.datarouter.web.browse.RoutersHandler;
import io.datarouter.web.browse.ViewNodeDataHandler;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.handler.IpDetectionHandler;
import io.datarouter.web.handler.TestApiHandler;
import io.datarouter.web.monitoring.DeploymentReportingHandler;
import io.datarouter.web.monitoring.ExecutorsMonitoringHandler;
import io.datarouter.web.monitoring.MemoryMonitoringHandler;
import io.datarouter.web.user.role.DatarouterUserRole;

@Singleton
public class DatarouterWebRouteSet extends BaseRouteSet{

	public static final String
			CLIENTS = "clients";

	public static final String
			PATH_datarouter = "/" + DatarouterWebPaths.DATAROUTER,
			PATH_clients = PATH_datarouter + "/" + CLIENTS;

	@Inject
	public DatarouterWebRouteSet(DatarouterWebPaths paths){
		super(paths.datarouter);
		handle(paths.datarouter.toSlashedString() + "|" + paths.datarouter.toSlashedString() + "/")
				.withHandler(RoutersHandler.class);
		// example: /datarouter/data/fadafa/adfadfafqe/abc or /datarouter/data/fadafa/adfadfafqe/abc.1341 or
		// /datarouter/data/fadafa/adfadfafqe/abbc_2152
		handle(paths.datarouter.data.toSlashedString() + REGEX_TWO_DIRECTORY_PLUS).withHandler(
				DatabeanViewerHandler.class);
		handleDir(paths.datarouter.memory).withHandler(MemoryMonitoringHandler.class);
		handleDir(paths.datarouter.executors).withHandler(ExecutorsMonitoringHandler.class);
		handleDir(paths.datarouter.nodes.browseData).withHandler(ViewNodeDataHandler.class);
		handle(paths.datarouter.nodes.deleteData).withHandler(DeleteNodeDataHandler.class);
		handle(paths.datarouter.nodes.getData).withHandler(GetNodeDataHandler.class);
		handle(paths.datarouter.clients).withHandler(DatarouterClientHandler.class);
		handle(paths.datarouter.ipDetection).withHandler(IpDetectionHandler.class).allowAnonymous();
		handle(paths.datarouter.deployment).withHandler(DeploymentReportingHandler.class).allowAnonymous();

		//example: /testApi or /testApidfadfa  or /testApi/ or /testApi/adfafa
		handleDir(paths.datarouter.testApi).withHandler(TestApiHandler.class);
	}

	@Override
	protected DispatchRule applyDefault(DispatchRule rule){
		return rule.allowRoles(DatarouterUserRole.datarouterAdmin);
	}

	public static class DatarouterWebRouteSetPackageTests extends BaseRouteSetPackageTests{

		public DatarouterWebRouteSetPackageTests(){
			super("io.datarouter.web.dispatcher.DatarouterWebRouteSet", DatarouterWebRouteSet.class);
		}

	}
}
