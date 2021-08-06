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
package io.datarouter.storage.dao;

/**
 * A DAO is where nodes are defined. The generic types on each node prevent you at compile time from accidentally saving
 * a databean to the wrong node or requesting a Databean with a PrimaryKey that doesn't match the node.
 *
 * Each DAO could be responsible for multiple physical repositories (for example mysql + sqs + memcached) but it could
 * be limited to a single Databean class. You can inject a DAO into application code for standard operations like get()
 * or putMulti(). Something that manipulates multiple databean classes or adds more complex logic could be named
 * “Service”.
 *
 * Pushing single-databean methods into the DAOs, even if they have complex logic, helps the reader understand the scope
 * of the logic, knowing from the start that it only touches that single databean. This delegation is particularly
 * helpful versus having large services where methods call each other, sometimes obfuscating their effects on multiple
 * tables.
 */
public interface Dao{
}
