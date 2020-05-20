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
package io.datarouter.model.entity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;

public class EntitySections<EK extends EntityKey<EK>>{

	private final Map<EntitySection,SortedSet<Object>> backingMap = new HashMap<>();

	public <PK extends EntityPrimaryKey<EK,PK>,D extends Databean<PK,D>> void add(String qualifierPrefix, D databean){
		backingMap.computeIfAbsent(new EntitySection(databean.getClass(), qualifierPrefix), $ -> new TreeSet<>())
				.add(databean);
	}

	public <PK extends EntityPrimaryKey<EK,PK>,D extends Databean<PK,D>> void addAll(
			String qualifierPrefix,
			Collection<D> databeans){
		databeans.stream()
				.findFirst()
				.map(Object::getClass)
				.map(databeanClass -> backingMap.computeIfAbsent(new EntitySection(databeanClass, qualifierPrefix),
						$ -> new TreeSet<>()))
				.ifPresent(section -> section.addAll(databeans));
	}

	@SuppressWarnings("unchecked")
	public <PK extends EntityPrimaryKey<EK,PK>,D extends Databean<PK,D>> SortedSet<D> get(
			String qualifierPrefix,
			Class<D> databeanClass){
		return (SortedSet<D>)backingMap.getOrDefault(new EntitySection(databeanClass, qualifierPrefix),
				Collections.emptySortedSet());
	}

	public long countDatabeans(){
		return backingMap.values().stream()
				.mapToInt(Collection::size)
				.sum();
	}

	private static class EntitySection{

		private final Class<?> databeanClass;
		private final String qualifierPrefix;

		private EntitySection(Class<?> databeanClass, String qualifierPrefix){
			this.databeanClass = databeanClass;
			this.qualifierPrefix = qualifierPrefix;
		}

		@Override
		public boolean equals(Object obj){
			if(!(obj instanceof EntitySection)){
				return false;
			}
			EntitySection other = (EntitySection)obj;
			return Objects.equals(databeanClass, other.databeanClass)
					&& Objects.equals(qualifierPrefix, other.qualifierPrefix);
		}

		@Override
		public int hashCode(){
			return Objects.hash(databeanClass, qualifierPrefix);
		}

	}

}
