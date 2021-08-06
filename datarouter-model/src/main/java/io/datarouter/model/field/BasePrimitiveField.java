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

import io.datarouter.util.ComparableTool;

public abstract class BasePrimitiveField<T extends Comparable<? super T>,K extends PrimitiveFieldKey<T,K>>
extends BaseField<T>{

	private final K key;

	public BasePrimitiveField(K key, T value){
		this(null, key, value);
	}

	public BasePrimitiveField(String prefix, K key, T value){
		super(prefix, value);
		this.key = key;
	}

	@Override
	public FieldKey<T> getKey(){
		return key;
	}

	@Override
	public int compareTo(Field<T> other){
		if(other == null){
			return 1;
		}
		return ComparableTool.nullFirstCompareTo(this.getValue(), other.getValue());
	}

}
