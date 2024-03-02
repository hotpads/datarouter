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
package io.datarouter.nodewatch.web.handler;

import io.datarouter.nodewatch.web.NodewatchLinks;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.GlobalRedirectMav;
import jakarta.inject.Inject;

public class NodewatchNodeNameHandler extends BaseHandler{

	public static final String
			P_nodeName = "nodeName";

	@Inject
	private NodewatchLinks links;
	@Inject
	private DatarouterNodes datarouterNodes;

	@Handler
	private Mav nodeName(String nodeName){
		PhysicalNode<?,?,?> physicalNode = datarouterNodes.getNode(nodeName).getPhysicalNodes().getFirst();
		String clientName = physicalNode.getClientId().getName();
		String tableName = physicalNode.getFieldInfo().getTableName();
		return new GlobalRedirectMav(links.table(clientName, tableName));
	}

}
