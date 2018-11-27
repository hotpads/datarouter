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

import io.datarouter.model.field.BaseField;
import io.datarouter.model.field.Field;
import io.datarouter.util.enums.DatarouterEnumTool;
import io.datarouter.util.enums.IntegerEnum;
import io.datarouter.util.varint.VarInt;

public class VarIntEnumField<E extends IntegerEnum<E>> extends BaseField<E>{

	private final VarIntEnumFieldKey<E> key;

	public VarIntEnumField(VarIntEnumFieldKey<E> key, E value){
		super(null, value);
		this.key = key;
		this.value = value;
	}

	@Override
	public VarIntEnumFieldKey<E> getKey(){
		return key;
	}

	@Override
	public int compareTo(Field<E> other){
		return DatarouterEnumTool.compareIntegerEnums(value, other.getValue());
	}

	@Override
	public String getStringEncodedValue(){
		if(value == null){
			return null;
		}
		return value.getPersistentInteger().toString();
	}

	@Override
	public E parseStringEncodedValueButDoNotSet(String string){
		if(string == null){
			return null;
		}
		return IntegerEnum.fromPersistentIntegerSafe(key.getSampleValue(), Integer.valueOf(string));
	}

	@Override
	public byte[] getBytes(){
		return value == null ? null : new VarInt(value.getPersistentInteger()).getBytes();
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return VarInt.fromByteArray(bytes, offset).getNumBytes();
	}

	@Override
	public E fromBytesButDoNotSet(byte[] bytes, int offset){
		return IntegerEnum.fromPersistentIntegerSafe(key.getSampleValue(), VarInt.fromByteArray(bytes, offset)
				.getValue());
	}

	@Override
	public String getValueString(){
		return value == null ? "null" : value.getPersistentInteger().toString();
	}

	public static <E extends IntegerEnum<E>> IntegerEnumField<E> toIntegerEnumField(VarIntEnumField<E> field){
		IntegerEnumField<E> integerEnumField = new IntegerEnumField<>(new IntegerEnumFieldKey<>(field.key.getName(),
				field.getKey().getEnumClass()), field.getValue());
		integerEnumField.setPrefix(field.getPrefix());
		return integerEnumField;
	}

}
