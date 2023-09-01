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
package io.datarouter.nodewatch.util;

import java.util.Optional;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientAndTableNames;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.op.combo.SortedMapStorage.PhysicalSortedMapStorageNode;
import io.datarouter.storage.util.PrimaryKeyPercentCodecTool;

public class PhysicalSortedNodeWrapper<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{

	public final PhysicalSortedMapStorageNode<PK,D,F> node;

	public PhysicalSortedNodeWrapper(PhysicalSortedMapStorageNode<PK,D,F> node){
		this.node = node;
	}

	@SuppressWarnings("unchecked")
	public PhysicalSortedNodeWrapper(DatarouterNodes datarouterNodes, String nodeName){
		this((PhysicalSortedMapStorageNode<PK,D,F>)datarouterNodes.getNode(nodeName));
	}

	@SuppressWarnings("unchecked")
	public PhysicalSortedNodeWrapper(DatarouterNodes datarouterNodes, String clientName, String tableName){
		this((PhysicalSortedMapStorageNode<PK,D,F>)datarouterNodes
				.getPhysicalNodeForClientAndTable(clientName, tableName));
	}

	@SuppressWarnings("unchecked")
	public PhysicalSortedNodeWrapper(DatarouterNodes datarouterNodes, ClientAndTableNames clientAndTableNames){
		this((PhysicalSortedMapStorageNode<PK,D,F>)datarouterNodes.getPhysicalNode(clientAndTableNames));
	}

	public PK parsePk(String stringKey){
		return PrimaryKeyPercentCodecTool.decode(
				node.getFieldInfo().getPrimaryKeySupplier(),
				stringKey);
	}

	public Optional<String> validatePk(String stringKey){
		try{
			parsePk(stringKey);
			return Optional.empty();
		}catch(RuntimeException e){
			return Optional.of("Couldn't parse PK");
		}
	}

}