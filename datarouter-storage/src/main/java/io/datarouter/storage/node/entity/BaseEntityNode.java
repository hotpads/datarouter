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
package io.datarouter.storage.node.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.node.Node;

public abstract class BaseEntityNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>>
implements EntityNode<EK,E>{

	protected final DatarouterProperties datarouterProperties;
	private Datarouter datarouter;
	private String name;
	private List<Node<?,?,?>> subEntityNodes;


	public BaseEntityNode(Datarouter datarouter, String name){
		this.datarouterProperties = datarouter.getDatarouterProperties();
		this.datarouter = datarouter;
		this.name = name;
		this.subEntityNodes = new ArrayList<>();
	}

	@Override
	public <PK extends EntityPrimaryKey<EK,PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>> void register(
			SubEntitySortedMapStorageReaderNode<EK,PK,D,F> subEntityNode){
		subEntityNodes.add(subEntityNode);
	}


	@Override
	public Datarouter getContext(){
		return datarouter;
	}

	@Override
	public String getName(){
		return name;
	}

	@Override
	public Collection<Node<?,?,?>> getSubEntityNodes(){
		return subEntityNodes;
	}

}
