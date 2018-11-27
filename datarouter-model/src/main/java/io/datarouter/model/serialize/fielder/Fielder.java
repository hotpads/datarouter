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
package io.datarouter.model.serialize.fielder;

import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldSet;

/**
 * A Fielder is a mapping of java fields to the columns and cells in the storage repository. A Databean can contain
 * multiple Fielders, for example, to store a Date in different formats in two different tables, or to facilitate
 * migration from one table schema to another.
 *
 * The Fielder is usually defined in the Databean as close to the java field definitions as possible. This is to reduce
 * the likelihood that someone will add a java field and forget to add it to all Fielders for the Databean.
 *
 * Even though a Fielder is defined in a Databean, the Databean isn't aware of the Fielder until you map them together
 * in a Node.
 */
public interface Fielder<F extends FieldSet<F>>{

	List<Field<?>> getFields(F fieldSet);

}
