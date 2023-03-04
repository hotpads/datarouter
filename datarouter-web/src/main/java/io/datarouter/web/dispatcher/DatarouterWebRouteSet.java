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
package io.datarouter.web.dispatcher;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.tag.Tag;
import io.datarouter.web.browse.DatarouterClientHandler;
import io.datarouter.web.browse.DatarouterHomepageHandler;
import io.datarouter.web.browse.DeleteNodeDataHandler;
import io.datarouter.web.browse.GetNodeDataHandler;
import io.datarouter.web.browse.NodeSearchHandler;
import io.datarouter.web.browse.ViewNodeDataHandler;
import io.datarouter.web.browse.ViewTableConfigurationHandler;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.digest.DailyDigestHandler;
import io.datarouter.web.email.EmailTestHandler;
import io.datarouter.web.handler.IpDetectionHandler;
import io.datarouter.web.handler.TestApiHandler;
import io.datarouter.web.http.HttpTestHandler;
import io.datarouter.web.inspect.DatarouterPropertiesViewHandler;
import io.datarouter.web.listener.DatarouterListenersViewHandler;
import io.datarouter.web.monitoring.DeploymentReportingHandler;
import io.datarouter.web.monitoring.ExecutorsMonitoringHandler;
import io.datarouter.web.monitoring.MemoryMonitoringHandler;
import io.datarouter.web.plugin.ViewPluginsHandler;
import io.datarouter.web.routeset.DatarouterRouteSetViewHandler;
import io.datarouter.web.shutdown.ShutdownHandler;
import io.datarouter.web.user.role.DatarouterUserRole;

@Singleton
public class DatarouterWebRouteSet extends BaseRouteSet{

	@Inject
	public DatarouterWebRouteSet(DatarouterWebPaths paths){
		handle(paths.datarouter.join("/", "/", "/") + "?")
				.withHandler(DatarouterHomepageHandler.class)
				.allowRoles(DatarouterUserRole.DATAROUTER_ACCOUNTS,
						DatarouterUserRole.DATAROUTER_JOB,
						DatarouterUserRole.DATAROUTER_MONITORING,
						DatarouterUserRole.DATAROUTER_SETTINGS,
						DatarouterUserRole.DATAROUTER_TOOLS);
		handle(paths.datarouter.memory.garbageCollector)
				.withHandler(MemoryMonitoringHandler.class)
				.allowRoles(DatarouterUserRole.DATAROUTER_MONITORING);
		handle(paths.datarouter.memory.view)
				.withHandler(MemoryMonitoringHandler.class)
				.allowRoles(DatarouterUserRole.DATAROUTER_MONITORING);
		handle(paths.datarouter.executors)
				.withHandler(ExecutorsMonitoringHandler.class)
				.allowRoles(DatarouterUserRole.DATAROUTER_MONITORING);
		handle(paths.datarouter.executors.getExecutors)
				.withHandler(ExecutorsMonitoringHandler.class)
				.allowRoles(DatarouterUserRole.DATAROUTER_MONITORING);
		handle(paths.datarouter.emailTest)
				.withHandler(EmailTestHandler.class)
				.allowRoles(DatarouterUserRole.DATAROUTER_TOOLS);
		handle(paths.datarouter.http.dnsLookup)
				.withHandler(HttpTestHandler.class)
				.allowRoles(DatarouterUserRole.DATAROUTER_TOOLS);
		handle(paths.datarouter.http.tester)
				.withHandler(HttpTestHandler.class)
				.allowRoles(DatarouterUserRole.DATAROUTER_TOOLS);
		handle(paths.datarouter.nodes.browseData).withHandler(ViewNodeDataHandler.class);
		handle(paths.datarouter.nodes.browseData.browseData).withHandler(ViewNodeDataHandler.class);
		handle(paths.datarouter.nodes.browseData.countKeys).withHandler(ViewNodeDataHandler.class);
		handle(paths.datarouter.nodes.deleteData).withHandler(DeleteNodeDataHandler.class);
		handle(paths.datarouter.nodes.getData).withHandler(GetNodeDataHandler.class);
		handle(paths.datarouter.nodes.search).withHandler(NodeSearchHandler.class);
		handle(paths.datarouter.tableConfiguration).withHandler(ViewTableConfigurationHandler.class);

		handle(paths.datarouter.client.initAllClients).withHandler(DatarouterClientHandler.class);
		handle(paths.datarouter.client.initClient).withHandler(DatarouterClientHandler.class);
		handle(paths.datarouter.client.inspectClient).withHandler(DatarouterClientHandler.class);

		handle(paths.datarouter.ipDetection).withHandler(IpDetectionHandler.class).allowAnonymous();
		handle(paths.datarouter.deployment).withHandler(DeploymentReportingHandler.class).allowAnonymous();
		handle(paths.datarouter.shutdown).withHandler(ShutdownHandler.class).allowAnonymous();

		handle(paths.datarouter.info.filterParams).withHandler(DatarouterServletFilterParamsViewHandler.class);
		handle(paths.datarouter.info.listeners).withHandler(DatarouterListenersViewHandler.class);
		handle(paths.datarouter.info.routeSets).withHandler(DatarouterRouteSetViewHandler.class);
		handle(paths.datarouter.info.plugins).withHandler(ViewPluginsHandler.class);
		handle(paths.datarouter.info.properties).withHandler(DatarouterPropertiesViewHandler.class);
		handle(paths.datarouter.dailyDigest.viewActionable).withHandler(DailyDigestHandler.class);
		handle(paths.datarouter.dailyDigest.viewSummary).withHandler(DailyDigestHandler.class);

		handle(paths.datarouter.testApi.before).withHandler(TestApiHandler.class);
		handle(paths.datarouter.testApi.year).withHandler(TestApiHandler.class);
		handle(paths.datarouter.testApi.now).withHandler(TestApiHandler.class);
		handle(paths.datarouter.testApi.length).withHandler(TestApiHandler.class);
		handle(paths.datarouter.testApi.size).withHandler(TestApiHandler.class);
		handle(paths.datarouter.testApi.count).withHandler(TestApiHandler.class);
		handle(paths.datarouter.testApi.first).withHandler(TestApiHandler.class);
		handle(paths.datarouter.testApi.hi).withHandler(TestApiHandler.class);
		handle(paths.datarouter.testApi.hello).withHandler(TestApiHandler.class);
		handle(paths.datarouter.testApi.banana).withHandler(TestApiHandler.class);
		handle(paths.datarouter.testApi.bananas).withHandler(TestApiHandler.class);
		handle(paths.datarouter.testApi.describe).withHandler(TestApiHandler.class);
		handle(paths.datarouter.testApi.sumInBase).withHandler(TestApiHandler.class);
		handle(paths.datarouter.testApi.printPrimitiveIntArray).withHandler(TestApiHandler.class);
		handle(paths.datarouter.testApi.printIntegerObjectArray).withHandler(TestApiHandler.class);
		handle(paths.datarouter.testApi.printPrimitiveIntArrayNoParamName).withHandler(TestApiHandler.class);
		handle(paths.datarouter.testApi.printComplicatedArrayParams).withHandler(TestApiHandler.class);
		handle(paths.datarouter.testApi.timeContains).withHandler(TestApiHandler.class);
	}

	@Override
	protected DispatchRule applyDefault(DispatchRule rule){
		return rule
				.allowRoles(DatarouterUserRole.DATAROUTER_ADMIN)
				.withTag(Tag.DATAROUTER);
	}

}
