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
package io.datarouter.web.config;

import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.BaseFieldKey;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldKeyAttribute;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;

public abstract class FieldKeyOverrider{

	public abstract void override();

	@SuppressWarnings("unchecked")
	protected <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> void setAttribute(
			FieldKeyAttribute<?> attribute,
			DatabeanFielder<PK,D> fielder,
			D databean){
		fielder.getFields(databean).stream()
				.map(Field::getKey)
				.map(BaseFieldKey.class::cast)
				.forEach(key -> key.with(attribute));
	}

	protected void setAttribute(FieldKeyAttribute<?> attribute, List<BaseFieldKey<?,?>> fieldKeys){
		fieldKeys.forEach(key -> key.with(attribute));
	}

}
