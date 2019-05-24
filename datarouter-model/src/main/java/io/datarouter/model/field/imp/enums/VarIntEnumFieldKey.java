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
package io.datarouter.model.field.imp.enums;

import io.datarouter.model.field.BaseFieldKey;
import io.datarouter.util.enums.IntegerEnum;
import io.datarouter.util.lang.ReflectionTool;

public class VarIntEnumFieldKey<E extends IntegerEnum<E>> extends BaseFieldKey<E>{

	private final Class<E> enumClass;
	private final E sampleValue;

	public VarIntEnumFieldKey(String name, Class<E> enumClass){
		super(name, enumClass);
		this.enumClass = enumClass;
		this.sampleValue = ReflectionTool.create(enumClass);
	}

	public Class<E> getEnumClass(){
		return enumClass;
	}

	public E getSampleValue(){
		return sampleValue;
	}

	@Override
	public VarIntEnumField<E> createValueField(final E value){
		return new VarIntEnumField<>(this, value);
	}

}
