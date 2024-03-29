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
package io.datarouter.model.key.primary;

import io.datarouter.model.key.unique.UniqueKey;
import io.datarouter.model.serialize.fielder.PrimaryKeyFielder;

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
 * Having a strongly-typed PrimaryKey defined for each table makes it easier to construct compound primary keys. PK
 * fields and their ordering is an important design decision for a large database table, and the PK class aims to
 * support compound primary keys without adding more work down the line for comparison, SQL generation, etc.
 *
 * To keep the application portable, avoid relying on the underlying datastore's automatic id generation. Unless you
 * need incrementing primary keys, it's usually more flexible to generate random IDs or rely on dedicated ID generator.
 */
public interface PrimaryKey<PK extends PrimaryKey<PK>>
extends UniqueKey<PK>, PrimaryKeyFielder<PK>{
}
