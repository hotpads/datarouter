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

import java.util.List;
import java.util.Optional;

import io.datarouter.gson.serialization.GsonTool;
import io.datarouter.model.field.imp.list.KeyedListField;

public abstract class BaseListField<
		V extends Comparable<V>,
		L extends List<V>,
		K extends ListFieldKey<V,L,K>>
extends KeyedListField<V,L,K>{

	public BaseListField(K key, L value){
		super(key, value);
	}

	@Override
	public String getStringEncodedValue(){
		return Optional.ofNullable(value)
				.map(GsonTool.GSON::toJson)
				.orElse(null);
	}

}
