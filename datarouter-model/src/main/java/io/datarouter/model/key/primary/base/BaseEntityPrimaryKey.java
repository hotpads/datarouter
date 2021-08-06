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
package io.datarouter.model.key.primary.base;

import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldTool;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.BasePrimaryKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.string.StringTool;

public abstract class BaseEntityPrimaryKey<
		EK extends EntityKey<EK>,
		PK extends EntityPrimaryKey<EK,PK>>
extends BasePrimaryKey<PK>
implements EntityPrimaryKey<EK,PK>{

	private static final String DEFAULT_ENTITY_KEY_FIELD_NAME = "entityKey";

	public String getEntityKeyName(){
		return DEFAULT_ENTITY_KEY_FIELD_NAME;
	}

	/*
	 * subclasses may override this to change column names
	 */
	@Override
	public List<Field<?>> getEntityKeyFields(){
		if(StringTool.isEmpty(getEntityKeyName())){//Should this logic be in FieldTool.prependPrefixes
			return getEntityKey().getFields();
		}
		return FieldTool.prependPrefixes(getEntityKeyName(), getEntityKey().getFields());
	}

	@Override
	public List<Field<?>> getFields(){
		return Scanner.of(getEntityKeyFields(), getPostEntityKeyFields()).concat(Scanner::of).list();
	}

}
