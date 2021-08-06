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
import java.util.SortedSet;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;

public abstract class BaseEntity<EK extends EntityKey<EK>> implements Entity<EK>, Comparable<BaseEntity<EK>>{

	private EK key;
	private EntitySections<EK> entitySections;

	public BaseEntity(EK key){
		this.key = key;
		this.entitySections = new EntitySections<>();
	}

	@Override
	public void setKey(EK key){
		this.key = key;
	}

	@Override
	public EK getKey(){
		return key;
	}

	@Override
	public int compareTo(BaseEntity<EK> entity){
		return getKey().compareTo(entity.getKey());
	}

	@Override
	public long getNumDatabeans(){
		return entitySections.countDatabeans();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <PK extends EntityPrimaryKey<EK,PK>,D extends Databean<PK,D>>
	void addDatabeansForQualifierPrefixUnchecked(String qualifierPrefix, Collection<? extends Databean<?,?>> databeans){
		addDatabeansForQualifierPrefix(qualifierPrefix, (Collection<D>)databeans);
	}

	public <PK extends EntityPrimaryKey<EK,PK>,D extends Databean<PK,D>>
	void addDatabeansForQualifierPrefix(String qualifierPrefix, Collection<D> databeans){
		entitySections.addAll(qualifierPrefix, databeans);
	}

	// custom table name
	public <PK extends EntityPrimaryKey<EK,PK>,D extends Databean<PK,D>>
	SortedSet<D> getDatabeansForQualifierPrefix(Class<D> databeanClass, String qualifierPrefix){
		return entitySections.get(qualifierPrefix, databeanClass);
	}

	public <PK extends EntityPrimaryKey<EK,PK>,D extends Databean<PK,D>>
	ArrayList<D> getListDatabeansForQualifierPrefix(Class<D> databeanClass, String qualifierPrefix){
		return new ArrayList<>(getDatabeansForQualifierPrefix(databeanClass, qualifierPrefix));
	}

}
