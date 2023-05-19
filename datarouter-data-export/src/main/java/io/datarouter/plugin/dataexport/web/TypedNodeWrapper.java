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
package io.datarouter.plugin.dataexport.web;

import java.util.Optional;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.op.raw.SortedStorage.SortedStorageNode;
import io.datarouter.storage.util.PrimaryKeyPercentCodecTool;

public class TypedNodeWrapper<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{

	public final SortedStorageNode<PK,D,F> node;

	public TypedNodeWrapper(SortedStorageNode<PK,D,F> node){
		this.node = node;
	}

	@SuppressWarnings("unchecked")
	public TypedNodeWrapper(DatarouterNodes datarouterNodes, String nodeName){
		this((SortedStorageNode<PK,D,F>)datarouterNodes.getNode(nodeName));
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