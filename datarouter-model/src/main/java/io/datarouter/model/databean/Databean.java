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
package io.datarouter.model.databean;

import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.key.primary.PrimaryKey;

/**
 * A Databean is an atomic unit of serialization corresponding to a MySQL row or a Memcached item. Generally, all fields
 * of the databean are read from the datastore together even if some are not wanted, and they are written to the
 * datastore together even if some are not updated.
 *
 * Every Databean has a single PrimaryKey which determines its uniqueness and ordering among other databeans of the same
 * type. This is determined by the hashCode(), equals(), and compareTo() methods in the PrimaryKey and should generally
 * not be modified.
 *
 * While Databeans consist of more code than JDO or JPA style databeans, they add many rich features such as: a strongly
 * typed PrimaryKey, equality, ordering, automatic schema updating, the ability to define multiple Fielders for
 * different storage formats, and the ability to serialize to arbitrary formats like JSON, Memcached, a file, or
 * HBase. Databeans are often the foundation of a project, and comprise a minority of the code so the trade-off is
 * usually worthwhile.
 */
public interface Databean<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends Comparable<Databean<?,?>>{

	String getDatabeanName();

	Class<PK> getKeyClass();
	String getKeyFieldName();
	PK getKey();

	List<Field<?>> getKeyFields();

}
