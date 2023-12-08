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
package io.datarouter.nodewatch.web;

import org.apache.http.client.utils.URIBuilder;

import io.datarouter.nodewatch.config.DatarouterNodewatchPaths;
import io.datarouter.nodewatch.web.handler.NodewatchTableHandler;
import io.datarouter.nodewatch.web.handler.NodewatchTableStorageHandler;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

// After the contextPath
@Singleton
public class NodewatchPublicLinks{

	@Inject
	private DatarouterNodewatchPaths paths;

	public String tables(){
		var uriBuilder = new URIBuilder()
				.setPath(paths.datarouter.nodewatch.tables.toSlashedString());
		return uriBuilder.toString();
	}

	public String table(String clientName, String tableName){
		var uriBuilder = new URIBuilder()
				.setPath(paths.datarouter.nodewatch.table.toSlashedString())
				.addParameter(NodewatchTableHandler.P_clientName, clientName)
				.addParameter(NodewatchTableHandler.P_tableName, tableName);
		return uriBuilder.toString();
	}

	public String tableStorage(String clientName, String tableName){
		var uriBuilder = new URIBuilder()
				.setPath(paths.datarouter.nodewatch.table.storage.toSlashedString())
				.addParameter(NodewatchTableStorageHandler.P_clientName, clientName)
				.addParameter(NodewatchTableStorageHandler.P_tableName, tableName);
		return uriBuilder.toString();
	}

}
