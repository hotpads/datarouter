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
package io.datarouter.model.key;

import io.datarouter.model.field.BaseFieldSet;

public abstract class BaseKey<K extends Key<K>>
extends BaseFieldSet<K>
implements Key<K>{

	public static final String NAME = "key";

//	@Override
//	public List<Field<?>> getKeyFields(){
//		return getFields();
//	}

//	@Override
//	public void fromPersistentString(String in){
//		String[] tokens = in.split("_");
//		int i=0;
//		for(Field<?> field : this.getFields()){
//			if(i>tokens.length-1){ break; }
//			field.fromString(tokens[i]);
//			field.setUsingReflection(this, field.getValue(), true);
//			field.setValue(null);//to be safe until Field logic is cleaned up
//			++i;
//		}
//	}

}
