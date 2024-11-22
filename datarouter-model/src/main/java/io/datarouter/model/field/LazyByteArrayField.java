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
package io.datarouter.model.field;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

import com.google.gson.reflect.TypeToken;

import io.datarouter.bytes.Codec;
import io.datarouter.bytes.Codec.NullPassthroughCodec;
import io.datarouter.util.singletonsupplier.SingletonSupplier;

public class LazyByteArrayField<T> implements Comparable<LazyByteArrayField<T>>{

	private final byte[] bytes;
	private final Supplier<T> valueSupplier;

	private LazyByteArrayField(byte[] bytes, Supplier<T> valueSupplier){
		this.bytes = bytes;
		this.valueSupplier = valueSupplier;
	}

	public LazyByteArrayField(Codec<T,byte[]> codec, byte[] bytes){
		this(bytes, SingletonSupplier.of(() -> NullPassthroughCodec.of(codec).decode(bytes)));
	}

	private LazyByteArrayField(Codec<T,byte[]> codec, T value){
		this(NullPassthroughCodec.of(codec).encode(value), () -> value);
	}

	public static <T> LazyByteArrayField<T> ofValue(Codec<T,byte[]> codec, T value){
		return new LazyByteArrayField<>(codec, value);
	}

	/*-------- Object -----------*/

	@Override
	public boolean equals(Object obj){
		return obj instanceof LazyByteArrayField
				&& Arrays.equals(bytes, ((LazyByteArrayField<?>)obj).bytes);
	}

	@Override
	public int hashCode(){
		return Arrays.hashCode(bytes);
	}

	@Override
	public String toString(){
		return Objects.toString(value());
	}

	/*------- Comparable ---------*/

	@Override
	public int compareTo(LazyByteArrayField<T> other){
		// Compare how the database would.
		return Arrays.compareUnsigned(bytes, other.bytes);
	}

	/*-------- TypeToken -------------*/

	@SuppressWarnings("unchecked")
	public static <T,L extends LazyByteArrayField<T>>
	TypeToken<LazyByteArrayField<T>> makeTypeToken(Class<T> valueClass){
		return (TypeToken<LazyByteArrayField<T>>)TypeToken.getParameterized(LazyByteArrayField.class, valueClass);
	}

	/*-------- LazyByteArrayField ----------*/

	public LazyByteArrayField<T> inflated(){
		valueSupplier.get();
		return this;
	}

	public byte[] bytes(){
		return bytes;
	}

	public T value(){
		return valueSupplier.get();
	}

}
