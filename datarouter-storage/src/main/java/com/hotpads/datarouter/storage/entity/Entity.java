package com.hotpads.datarouter.storage.entity;

import java.util.Collection;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;

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
	
	int getNumDatabeans();
}
