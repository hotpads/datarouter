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
package io.datarouter.bytes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class KvString{

	private record Kv<T>(
			String key,
			Supplier<T> valueSupplier,
			Function<T,String> toStringFn){

		@Override
		public String toString(){
			T value = valueSupplier.get();
			String stringValue = value == null ? "" : toStringFn.apply(value);
			return String.join("=", key, stringValue);
		}
	}

	private final List<Kv<?>> kvs;

	public KvString(){
		kvs = new ArrayList<>();
	}

	public <T> KvString addLazy(String name, Supplier<T> valueSupplier, Function<T,String> toStringFn){
		kvs.add(new Kv<>(name, valueSupplier, toStringFn));
		return this;
	}

	public KvString addLazy(String name, Supplier<String> value){
		return addLazy(name, value, Function.identity());
	}

	public <T> KvString add(String name, T value, Function<T,String> toStringFn){
		return addLazy(name, () -> value, toStringFn);
	}

	public KvString add(String name, String value){
		return addLazy(name, () -> value);
	}

	@Override
	public String toString(){
		return kvs.stream()
				.map(Kv::toString)
				.collect(Collectors.joining(", "));

	}

}
