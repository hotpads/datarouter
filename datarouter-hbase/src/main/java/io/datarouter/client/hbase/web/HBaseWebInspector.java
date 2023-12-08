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
package io.datarouter.client.hbase.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import io.datarouter.client.hbase.config.DatarouterHBaseFiles;
import io.datarouter.client.hbase.config.DatarouterHBasePaths;
import io.datarouter.pathnode.PathNode;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.storage.client.ClientType;
import io.datarouter.web.browse.DatarouterClientWebInspector;
import io.datarouter.web.browse.dto.DatarouterWebRequestParamsFactory;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.handler.params.Params;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class HBaseWebInspector implements DatarouterClientWebInspector{

	@Inject
	private ClientOptions clientOptions;
	@Inject
	private DatarouterHBaseFiles files;
	@Inject
	private DatarouterHBasePaths datarouterHBasePaths;
	@Inject
	private DatarouterWebRequestParamsFactory paramsFactory;

	@Override
	public Mav inspectClient(Params params, HttpServletRequest request){
		var clientParams = paramsFactory.new DatarouterWebRequestParams<>(params, ClientType.class);
		var clientId = clientParams.getClientId();
		if(clientId == null){
			return new MessageMav("Client not found");
		}

		var clientName = clientId.getName();
		Map<String,String> allClientOptions = clientOptions.getAllClientOptions(clientName);
		var clientPageHeader = buildClientPageHeader(clientName);
		var clientOptionsTable = buildClientOptionsTable(allClientOptions);

		//TODO return j2html page instead of jsp
		Mav mav = new Mav();
		mav.setViewName(files.jsp.admin.datarouter.hbase.hbaseClientSummaryJsp);
		mav.put("clientPageHeader", clientPageHeader.render());
		mav.put("clientOptionsTable", clientOptionsTable.render());
		return mav;
	}

	protected PathNode getHandlerPath(){
		return datarouterHBasePaths.datarouter.clients.hbase;
	}

}
