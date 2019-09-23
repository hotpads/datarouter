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
package io.datarouter.storage.router;

/**
 * A Router is a strongly-typed collection of Nodes. It is easy to trace all uses of the node through your code using
 * tools like Eclipse's ctrl-shift-G shortcut. The generic types on each node prevent you at compile time from
 * accidentally saving a databean to the wrong node or requesting a Databean with a PrimaryKey that doesn't match the
 * node.
 *
 * You can inject a router into application code for standard operations like get() or putMulti(), or you can inject it
 * into a DAO layer that adds more complex logic.
 *
 * Nodes are instantiated in the router, and the router is where each PhysicalNode is mapped to a Client.
 *
 * While a small application may have only one router, a large application may group related nodes into separate
 * routers. A good rule of thumb is to have one router per database giving access to all tables in the database. It is
 * also feasible to have a router talk to multiple clients/databases, or to have multiple routers talking to the same
 * client.
 *
 * The router does not "own" a Client, it merely keeps a ClientId reference to each Client. Clients are owned by
 * Datarouter, and there is no penalty for using a Client in multiple routers.
 */
public interface Router{
}
