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
package io.datarouter.web.browse;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.NodeTool;
import io.datarouter.web.browse.dto.NodeWrapper;
import io.datarouter.web.handler.BaseHandler;

public class NodeSearchHandler extends BaseHandler{

	@Inject
	private DatarouterNodes datarouterNodes;

	@Handler(defaultHandler = true)
	public List<NodeWrapper> search(String keyword){
		List<Node<?,?,?>> nodes = datarouterNodes.getAllNodes().stream()
				.map(NodeTool::getUnderlyingNode)
				.filter(node -> node.getName().toLowerCase().contains(keyword.toLowerCase()))
				.map(node -> datarouterNodes.getTopLevelNodes().stream()
						.filter(topLevelNode -> NodeTool.getNodeAndDescendants(topLevelNode).stream()
								.map(NodeTool::getUnderlyingNode)
								.anyMatch(node::equals))
						.findAny()
						.orElseThrow(() -> new RuntimeException(node + "")))
				.distinct()
				.collect(Collectors.toList());
		return NodeWrapper.getNodeWrappers(nodes);
	}

}
