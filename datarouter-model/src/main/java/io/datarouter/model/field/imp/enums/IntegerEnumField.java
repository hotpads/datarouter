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
import io.datarouter.util.bytes.IntegerByteTool;
import io.datarouter.util.enums.DatarouterEnumTool;
import io.datarouter.util.enums.IntegerEnum;

public class IntegerEnumField<E extends IntegerEnum<E>> extends BaseField<E>{

	private final IntegerEnumFieldKey<E> key;

	public IntegerEnumField(IntegerEnumFieldKey<E> key, E value){
		super(null, value);
		this.key = key;
	}

	@Override
	public IntegerEnumFieldKey<E> getKey(){
		return key;
	}

	public E getSampleValue(){
		return key.getSampleValue();
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
	public E parseStringEncodedValueButDoNotSet(String str){
		if(str == null){
			return null;
		}
		return IntegerEnum.fromPersistentIntegerSafe(getSampleValue(), Integer.valueOf(str));
	}

	@Override
	public byte[] getBytes(){
		return value == null ? null : IntegerByteTool.getComparableBytes(value.getPersistentInteger());
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 4;
	}

	@Override
	public E fromBytesButDoNotSet(byte[] bytes, int offset){
		return IntegerEnum.fromPersistentIntegerSafe(
				getSampleValue(),
				IntegerByteTool.fromComparableBytes(bytes, offset));
	}

	@Override
	public String getValueString(){
		return value == null ? "null" : value.getPersistentInteger().toString();
	}

}
