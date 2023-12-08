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
package io.datarouter.model.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;

public class EntityTool{

	public static <EK extends EntityKey<EK>,E extends Entity<EK>> Map<EK,E> getByKey(Collection<E> entities){
		return entities.stream()
				.collect(Collectors.toMap(Entity::getKey, Function.identity()));
	}

	public static <EK extends EntityKey<EK>,PK extends EntityPrimaryKey<EK,PK>>
	NavigableMap<EK,List<PK>> getPrimaryKeysByEntityKey(Iterable<PK> pks){
		NavigableMap<EK,List<PK>> pksByEntityKey = new TreeMap<>();
		for(PK pk : pks){
			EK ek = pk.getEntityKey();
			List<PK> pksForEntity = pksByEntityKey.computeIfAbsent(ek, $ -> new ArrayList<>());
			pksForEntity.add(pk);
		}
		return pksByEntityKey;
	}

	public static <EK extends EntityKey<EK>,PK extends EntityPrimaryKey<EK,PK>,D extends Databean<PK,D>>
	NavigableMap<EK,List<D>> getDatabeansByEntityKey(Iterable<D> databeans){
		NavigableMap<EK,List<D>> databeansByEntityKey = new TreeMap<>();
		for(D databean : databeans){
			if(databean == null){
				continue;
			}// seem to be getting some null entries from TraceFlushController?
			PK pk = databean.getKey();//leave on individual line for NPE trace
			EK ek = pk.getEntityKey();
			List<D> databeansForEntity = databeansByEntityKey.computeIfAbsent(ek, $ -> new ArrayList<>());
			databeansForEntity.add(databean);
		}
		return databeansByEntityKey;
	}

}
