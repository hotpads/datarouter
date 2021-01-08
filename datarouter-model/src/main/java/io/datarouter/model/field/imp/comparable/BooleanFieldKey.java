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
package io.datarouter.model.field.imp.comparable;

import java.util.Map;

import io.datarouter.model.field.FieldKeyAttribute;
import io.datarouter.model.field.FieldKeyAttributeKey;
import io.datarouter.model.field.PrimitiveFieldKey;
import io.datarouter.model.field.encoding.FieldGeneratorType;

public class BooleanFieldKey extends PrimitiveFieldKey<Boolean,BooleanFieldKey>{

	public BooleanFieldKey(String name){
		super(name, Boolean.class);
	}

	private BooleanFieldKey(
			String name,
			String columnName,
			boolean nullable,
			FieldGeneratorType fieldGeneratorType,
			Boolean defaultValue,
			Map<FieldKeyAttributeKey<?>,FieldKeyAttribute<?>> attributes){
		super(name, columnName, nullable, Boolean.class, fieldGeneratorType, defaultValue, attributes);
	}

	public BooleanFieldKey withDefaultValue(Boolean defaultValueOverride){
		return new BooleanFieldKey(name, columnName, nullable, fieldGeneratorType, defaultValueOverride, attributes);
	}

	public BooleanFieldKey withColumnName(String columnNameOverride){
		return new BooleanFieldKey(name, columnNameOverride, nullable, fieldGeneratorType, defaultValue, attributes);
	}

	@Override
	public BooleanField createValueField(Boolean value){
		return new BooleanField(this, value);
	}

}
