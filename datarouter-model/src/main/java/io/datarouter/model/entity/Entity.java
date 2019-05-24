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

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;

/**
 * An Entity is a grouping of related databeans that are stored within the same transaction scope.
 *
 * For example, a User Entity may be comprised of several databean types: a single UserInfo Databean and a collection of
 * UserRole databeans. In a partitioned RDBMS, the Entity will span two tables per partition, but data for a single User
 * will reside in the same partition. In schema-less datastores, all databeans for the Entity can be serialized into an
 * atomic unit like a single Memcached object or a single HBase row. All databeans in the Entity can be fetched together
 * with a single call to entityNode.get(entityKey). For the RDBMS, this will execute a single txn with two select
 * operations. For Memcached or HBase the get() will execute in a single operation.
 *
 * The Entity facilitates passing around a group of related Databeans as a single java object.
 */
public interface Entity<EK extends EntityKey<EK>>{

	void setKey(EK ek);
	EK getKey();

	<PK extends EntityPrimaryKey<EK,PK>,D extends Databean<PK,D>>
	void addDatabeansForQualifierPrefixUnchecked(String subEntityTableName,
			Collection<? extends Databean<?,?>> databeans);

	long getNumDatabeans();

	default boolean notEmpty(){
		return getNumDatabeans() > 0;
	}

}
