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
package io.datarouter.model.databean;

import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldTool;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.util.PercentFieldCodec;
import io.datarouter.util.lang.ClassTool;

public abstract class BaseDatabean<PK extends PrimaryKey<PK>,D extends Databean<PK,D>> implements Databean<PK,D>{

	public static final String DEFAULT_KEY_FIELD_NAME = "key";

	private final PK key;

	public BaseDatabean(PK key){
		this.key = key;
	}

	@Override
	public final PK getKey(){
		return key;
	}

	@Override
	public String getDatabeanName(){
		return getClass().getSimpleName();
	}

	@Override
	public String getKeyFieldName(){
		return DEFAULT_KEY_FIELD_NAME;
	}

	@Override
	public List<Field<?>> getKeyFields(){
		return FieldTool.prependPrefixes(getKeyFieldName(), getKey().getFields());
	}

	@Override
	public boolean equals(Object obj){
		if(ClassTool.differentClass(this, obj)){
			return false;
		}
		@SuppressWarnings("unchecked")
		D that = (D)obj;
		return getKey().equals(that.getKey());
	}

	@Override
	public int hashCode(){
		return getKey().hashCode();
	}

	@Override
	public int compareTo(Databean<?,?> that){
		int diff = ClassTool.compareClass(this, that);
		if(diff != 0){
			return diff;
		}
		@SuppressWarnings("unchecked")
		D other = (D)that;
		return getKey().compareTo(other.getKey());
	}

	@Override
	public String toString(){
		return getClass().getSimpleName() + "." + PercentFieldCodec.encodeFields(getKey().getFields());
	}

}
