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
package io.datarouter.client.mysql.web;

import javax.inject.Inject;

import io.datarouter.client.mysql.MysqlClientManager;
import io.datarouter.client.mysql.MysqlClientType;
import io.datarouter.inject.DatarouterInjector;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.web.browse.DatarouterClientWebInspector;
import io.datarouter.web.browse.dto.DatarouterWebRequestParamsFactory;
import io.datarouter.web.browse.dto.DatarouterWebRequestParamsFactory.DatarouterWebRequestParams;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.params.Params;

public class MysqlWebInspector implements DatarouterClientWebInspector{

	@Inject
	private DatarouterNodes nodes;
	@Inject
	private DatarouterWebRequestParamsFactory datarouterWebRequestParamsFactory;
	@Inject
	private DatarouterMysqlFiles files;
	@Inject
	private DatarouterInjector injector;

	@Override
	public Mav inspectClient(Params params){
		DatarouterWebRequestParams<MysqlClientType> nodeParams = datarouterWebRequestParamsFactory
				.new DatarouterWebRequestParams<>(params, MysqlClientType.class);
		Mav mav = new Mav(files.jsp.admin.datarouter.mysql.mysqlClientSummaryJsp);
		MysqlClientManager clientManager = injector.getInstance(nodeParams.getClientType().getClientManagerClass());
		mav.put("clientStats", clientManager.getStats(nodeParams.getClientId()));
		mav.put("nodes", nodes.getPhysicalNodesForClient(nodeParams.getClientId().getName()));
		return mav;
	}

}