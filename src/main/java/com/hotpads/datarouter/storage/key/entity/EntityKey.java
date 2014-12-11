package com.hotpads.datarouter.storage.key.entity;

import com.hotpads.datarouter.storage.key.Key;

/**
 * 
 * @author mcorgan
 * 
 *         EntityKey defines a unique key for a collection of related databeans. Any databeans in the Entity will have a
 *         PrimaryKey whose leftmost fields are defined by the EntityKey. The PrimaryKeys are then distinguished by
 *         their type and any additional fields they add to the EntityKey.
 * 
 * @param <K>
 */
public interface EntityKey<K extends Key<K>>
extends Key<K>{
	
}
