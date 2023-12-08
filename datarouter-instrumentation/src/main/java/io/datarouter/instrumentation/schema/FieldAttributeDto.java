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
package io.datarouter.instrumentation.schema;

import java.util.Objects;

public class FieldAttributeDto{

	public final String name;
	public final String value;

	public FieldAttributeDto(String name, String value){
		this.name = name;
		this.value = value;
	}

	@Override
	public boolean equals(Object obj){
		if(obj instanceof FieldAttributeDto that){
			return this.name.equals(that.name) && this.value.equals(that.value);
		}else{
			return false;
		}
	}

	@Override
	public int hashCode(){
		return Objects.hash(this.name, this.value);
	}

}
