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
package io.datarouter.bytes.binarydto.dto;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

import io.datarouter.bytes.binarydto.codec.BinaryDtoComparableCodec;
import io.datarouter.bytes.binarydto.internal.BinaryDtoFieldSchema;

public abstract class ComparableBinaryDto<T extends ComparableBinaryDto<T>>
extends BaseBinaryDto<T>
implements Comparable<T>{

	/*-------------- codec ---------------*/

	private final BinaryDtoComparableCodec<T> comparableCodec(){
		return BinaryDtoComparableCodec.of(getClass());
	}

	@SuppressWarnings("unchecked")
	public final byte[] encodeComparable(){
		return comparableCodec().encode((T)this);
	}

	public final T cloneComparable(){
		return comparableCodec().decode(encodeComparable());
	}

	/*----------- fields -----------*/

	@Override
	public final List<Field> getFieldsOrdered(){
		return comparableCodec().getFieldsOrdered();
	}

	/*-------------- Comparable -------------*/

	@Override
	public final int compareTo(T that){
		Objects.requireNonNull(that);
		if(!getClass().equals(that.getClass())){
			String message = String.format("Cannot compare %s to %s",
					getClass().getCanonicalName(),
					that.getClass().getCanonicalName());
			throw new IllegalArgumentException(message);
		}
		BinaryDtoComparableCodec<T> codec = comparableCodec();
		for(BinaryDtoFieldSchema<?> fieldSchema : codec.fieldSchemas){
			int fieldDiff = fieldSchema.compareFieldValuesAsIfEncoded(this, that);
			if(fieldDiff != 0){
				return fieldDiff;
			}
		}
		return 0;
	}

}
