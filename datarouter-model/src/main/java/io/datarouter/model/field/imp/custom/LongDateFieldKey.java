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
package io.datarouter.model.field.imp.custom;

import java.util.Date;
import java.util.Map;

import io.datarouter.model.field.FieldKeyAttribute;
import io.datarouter.model.field.FieldKeyAttributeKey;
import io.datarouter.model.field.PrimitiveFieldKey;
import io.datarouter.model.field.encoding.FieldGeneratorType;

public class LongDateFieldKey extends PrimitiveFieldKey<Date,LongDateFieldKey>{

	public LongDateFieldKey(String name){
		super(name, Date.class);
	}

	private LongDateFieldKey(
			String name,
			String columnName,
			boolean nullable,
			FieldGeneratorType fieldGeneratorType,
			Date defaultValue,
			Map<FieldKeyAttributeKey<?>,FieldKeyAttribute<?>> attributes){
		super(name, columnName, nullable, Date.class, fieldGeneratorType, defaultValue, attributes);
	}

	public LongDateFieldKey withColumnName(String columnName){
		return new LongDateFieldKey(name, columnName, nullable, fieldGeneratorType, defaultValue, attributes);
	}

}
