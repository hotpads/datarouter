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
package io.datarouter.gcp.spanner.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.datarouter.gcp.spanner.SpannerClientType;
import io.datarouter.instrumentation.test.TestableService;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.op.IndexedOps;
import io.datarouter.storage.node.tableconfig.ClientTableEntityPrefixNameWrapper;
import io.datarouter.storage.node.type.index.ManagedNode;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.util.string.StringTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterSpannerBootstrapIntegrationService implements TestableService{

	@Inject
	private DatarouterNodes nodes;

	@Override
	public void testAll(){
		testSpannerObjectNameUniqueness();
	}

	private void testSpannerObjectNameUniqueness(){
		Scanner.of(nodes.getAllNodes())
				.concatIter(Node::getPhysicalNodes)
				// filter out duplicates due to replication nodes and cached nodes
				.distinct()
				.include(node -> node.getClientId().getName().equals(SpannerClientType.NAME))
				.flush(this::assertSpannerObjectNameUniqueness);
	}

	private void assertSpannerObjectNameUniqueness(Collection<? extends PhysicalNode<?,?,?>> spannerNodes){
		Set<String> objectNamesLowercase = new HashSet<>();

		spannerNodes.forEach(node -> {
			List<String> nodeObjectNames = new ArrayList<>();
			nodeObjectNames.add(getNodeNameWithoutClientIdPrefix(node));
			if(node instanceof IndexedOps<?,?> indexedNode){
				nodeObjectNames.addAll(Scanner.of(indexedNode.getManagedNodes())
						.map(ManagedNode::getName)
						.list());
			}
			nodeObjectNames.forEach(objectName -> {
				if(objectNamesLowercase.contains(objectName.toLowerCase())){
					throw new IllegalStateException("Found duplicate spanner object name: " + objectName);
				}
				objectNamesLowercase.add(objectName.toLowerCase());
			});
		});
	}

	private String getNodeNameWithoutClientIdPrefix(PhysicalNode<?,?,?> node){
		return StringTool.splitOnCharNoRegex(node.getName(), ClientTableEntityPrefixNameWrapper.DELIMITER).get(1);
	}

}
