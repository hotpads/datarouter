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
package io.datarouter.model.field.imp.array;

import java.util.List;

import io.datarouter.model.field.BaseField;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.ListFieldKey;

public abstract class KeyedListField<
		V extends Comparable<V>,
		L extends List<V>, //value must be a List that implements a deep hashCode
		K extends ListFieldKey<V,L,K>>
extends BaseField<L>{

	protected final K key;

	public KeyedListField(K key, L value){
		super(null,value);
		this.key = key;
	}

	public KeyedListField(String prefix, K key, L value){
		super(prefix, value);
		this.key = key;
	}

	@Override
	public K getKey(){
		return key;
	}

	@Override
	public int compareTo(Field<L> other){
		if(other == null){
			return 1;
		}
		return this.toString().compareTo(other.toString());
	}

	public L getValues(){
		return value;
	}

	public int size(){
		return value == null ? 0 : value.size();
	}

}
