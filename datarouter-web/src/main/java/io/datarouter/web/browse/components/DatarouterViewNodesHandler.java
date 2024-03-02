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
package io.datarouter.web.browse.components;

import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h5;

import java.util.List;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.type.index.ManagedNodesHolder;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import jakarta.inject.Inject;

public class DatarouterViewNodesHandler extends BaseHandler{

	@Inject
	private DatarouterWebPaths paths;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterNodes datarouterNodes;
	@Inject
	private DatarouterClients datarouterClients;
	@Inject
	private ManagedNodesHolder managedNodesHolder;

	@Handler
	public Mav nodes(){
		var header = DatarouterComponentsHtml.makeHeader(
				paths.datarouter.info.nodes,
				"Nodes",
				"Nodes map to database tables or other storage containers");
		List<Node<?,?,?>> physicalNodes = Scanner.of(datarouterNodes.getAllNodes())
				.include(node -> node instanceof PhysicalNode)
				.list();
		List<Node<?,?,?>> virtualNodes = Scanner.of(datarouterNodes.getAllNodes())
				.exclude(node -> node instanceof PhysicalNode)
				.list();
		var physicalNodesTable = new J2HtmlTable<Node<?,?,?>>()
				.withClasses("table table-sm table-striped border")
				.withColumn("Node Name", Node::getName)
				.withColumn("Client Type", this::getClientTypeName)
				.withColumn("Client Name", this::getClientName)
				.withColumn(
						"Indexes",
						node -> {
							int numManagedNodes = managedNodesHolder.getManagedNodes((PhysicalNode<?,?,?>)node).size();
							return numManagedNodes == 0 ? "" : NumberFormatter.addCommas(numManagedNodes);
						})
				.build(physicalNodes);
		var virtualNodesTable = new J2HtmlTable<Node<?,?,?>>()
				.withClasses("table table-sm table-striped border")
				.withColumn("Node Name", Node::getName)
				.build(virtualNodes);
		var content = div(
				header,
				br(),
				h5("Physical Nodes"),
				physicalNodesTable,
				br(),
				h5("Virtual Nodes"),
				virtualNodesTable)
				.withClass("container");
		return pageFactory.startBuilder(request)
				.withTitle("Datarouter Nodes")
				.withContent(content)
				.buildMav();
	}

	private String getClientName(Node<?,?,?> node){
		return node.getClientIds().getFirst().getName();
	}

	private String getClientTypeName(Node<?,?,?> node){
		return datarouterClients.getClientTypeInstance(node.getClientIds().getFirst()).getName();
	}

}
