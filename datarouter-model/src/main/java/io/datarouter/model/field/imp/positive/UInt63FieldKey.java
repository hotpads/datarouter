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
package io.datarouter.model.field.imp.positive;

import java.util.Map;

import io.datarouter.model.field.FieldKeyAttribute;
import io.datarouter.model.field.FieldKeyAttributeKey;
import io.datarouter.model.field.PrimitiveFieldKey;
import io.datarouter.model.field.encoding.FieldGeneratorType;
import io.datarouter.util.number.RandomTool;

/**
 * @deprecated use LongFieldKey instead
 */
@Deprecated
public class UInt63FieldKey extends PrimitiveFieldKey<Long,UInt63FieldKey>{

	public UInt63FieldKey(String name){
		super(name, Long.class);
	}

	private UInt63FieldKey(
			String name,
			String columnName,
			boolean nullable,
			FieldGeneratorType fieldGeneratorType,
			Long defaultValue,
			Map<FieldKeyAttributeKey<?>,FieldKeyAttribute<?>> attributes){
		super(name, columnName, nullable, Long.class, fieldGeneratorType, defaultValue, attributes);
	}

	public UInt63FieldKey withFieldGeneratorType(FieldGeneratorType fieldGeneratorTypeOverride){
		return new UInt63FieldKey(name, columnName, nullable, fieldGeneratorTypeOverride, defaultValue, attributes);
	}

	public UInt63FieldKey withColumnName(String columnNameOverride){
		return new UInt63FieldKey(name, columnNameOverride, nullable, fieldGeneratorType, defaultValue, attributes);
	}

	public UInt63FieldKey withNullable(boolean nullableOverride){
		return new UInt63FieldKey(name, columnName, nullableOverride, fieldGeneratorType, defaultValue, attributes);
	}

	@Override
	public UInt63Field createValueField(Long value){
		return new UInt63Field(this, value);
	}

	@Override
	public Long generateRandomValue(){
		return RandomTool.nextPositiveLong();
	}

}
