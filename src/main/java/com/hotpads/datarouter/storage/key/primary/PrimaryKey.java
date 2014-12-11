package com.hotpads.datarouter.storage.key.primary;


import com.hotpads.datarouter.serialize.fielder.PrimaryKeyFielder;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;

/**
 * A primary key is an ordered set of fields that uniquely identify a Databean among others of the same type. It
 * corresponds to MySQL's primary key or a Memcached key.
 * 
 * The PrimaryKey defines the hashCode(), equals() and compareTo() methods that distinguish databeans. The user should
 * generally not override these methods. They are based on the PK fields, in order, and allow java collections to mimic
 * the behavior of the underlying datastore. For example, adding a Databean to a TreeSet will insert it in the same
 * order that MySQL inserts it into the database. If the Databean already exists in the set, then calling
 * treeSet.put(databean) will overwrite the existing Databean in the set, similar to updating the Databean in the
 * database.
 * 
 * @author mcorgan
 * 
 * @param <PK>
 */
public interface PrimaryKey<PK extends PrimaryKey<PK>>
extends UniqueKey<PK>, PrimaryKeyFielder<PK>{

	PrimaryKey<PK> getPrimaryKey();
	
}
