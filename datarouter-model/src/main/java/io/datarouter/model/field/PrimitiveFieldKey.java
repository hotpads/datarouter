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
package io.datarouter.model.field;

import java.util.Map;

import io.datarouter.model.field.encoding.FieldGeneratorType;

public abstract class PrimitiveFieldKey<T extends Comparable<? super T>,K extends PrimitiveFieldKey<T,K>>
extends BaseFieldKey<T,K>{

	public PrimitiveFieldKey(String name, Class<T> valueType){
		super(name, valueType);
	}

	protected PrimitiveFieldKey(String name, String columnName, boolean nullable, Class<T> valueType,
			FieldGeneratorType fieldGeneratorType, T defaultValue,
			Map<FieldKeyAttributeKey<?>,FieldKeyAttribute<?>> attributes){
		super(name, columnName, nullable, valueType, fieldGeneratorType, defaultValue, attributes);
	}

}