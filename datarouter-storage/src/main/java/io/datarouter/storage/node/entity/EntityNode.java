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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.serialize.fieldcache.EntityFieldInfo;

public interface EntityNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>>{

	String getName();

	<PK extends EntityPrimaryKey<EK,PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
	void register(SubEntitySortedMapStorageReaderNode<EK,PK,D,F> subEntityNode);

	Collection<Node<?,?,?>> getSubEntityNodes();

	/*------------ getEntities ---------------*/

	List<E> getEntities(Collection<EK> entityKeys, Config config);

	default List<E> getEntities(Collection<EK> entityKeys){
		return getEntities(entityKeys, new Config());
	}

	/*------------ getEntity ---------------*/

	default E getEntity(EK entityKey, Config config){
		return getEntities(Collections.singletonList(entityKey), config).stream().findFirst().orElse(null);
	}

	default E getEntity(EK entityKey){
		return getEntity(entityKey, new Config());
	}

	/*------------ deleteMultiEntities ---------------*/

	void deleteMultiEntities(Collection<EK> entityKeys, Config config);

	default void deleteMultiEntities(Collection<EK> entityKeys){
		deleteMultiEntities(entityKeys, new Config());
	}

	/*------------ deleteEntity ---------------*/

	default void deleteEntity(EK entityKey, Config config){
		deleteMultiEntities(Arrays.asList(entityKey), config);
	}

	default void deleteEntity(EK entityKey){
		deleteEntity(entityKey, new Config());
	}

	/*------------ listEntityKeys ---------------*/

	List<EK> listEntityKeys(EK startKey, boolean startKeyInclusive, Config config);

	default List<EK> listEntityKeys(EK startKey, boolean startKeyInclusive){
		return listEntityKeys(startKey, startKeyInclusive, new Config());
	}

	EntityFieldInfo<EK,E> getEntityFieldInfo();
}
