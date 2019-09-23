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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.datarouter.util.collection.CollectionTool;

public class SimpleFieldSet<F extends FieldSet<F>> extends BaseFieldSet<F>{

	protected List<Field<?>> fields = new ArrayList<>();

	public SimpleFieldSet(){
	}

	public SimpleFieldSet(Collection<Field<?>> fields){
		add(fields);
	}

	public void add(Field<?> field){
		if(field == null){
			return;
		}
		this.fields.add(field);
	}

	public SimpleFieldSet<?> add(Collection<Field<?>> fields){
		for(Field<?> field : CollectionTool.nullSafe(fields)){
			this.add(field);
		}
		return this;
	}

	@Override
	public List<Field<?>> getFields(){
		return fields;
	}

	public Field<?> getFirst(){
		return CollectionTool.getFirst(fields);
	}

}
