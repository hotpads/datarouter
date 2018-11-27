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

import io.datarouter.client.mysql.MysqlClient;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.web.browse.DatarouterClientWebInspector;
import io.datarouter.web.browse.dto.RouterParamsFactory;
import io.datarouter.web.browse.dto.RouterParamsFactory.RouterParams;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.params.Params;

public class MysqlWebInspector implements DatarouterClientWebInspector{

	@Inject
	private DatarouterNodes nodes;
	@Inject
	private RouterParamsFactory routerParamsFactory;
	@Inject
	private DatarouterMysqlFiles files;

	@Override
	public Mav inspectClient(Params params){
		RouterParams<MysqlClient> routerParams = routerParamsFactory.new RouterParams<>(params, MysqlClient.class);
		Mav mav = new Mav(files.jsp.admin.datarouter.mysql.mysqlClientSummaryJsp);
		mav.put("clientStats", routerParams.getClient().getStats());
		mav.put("nodes", nodes.getPhysicalNodesForClient(routerParams.getClientName()));
		return mav;
	}

}