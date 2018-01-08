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
package io.datarouter.model.serialize.fielder;

import java.util.Objects;

public class FielderConfigKey<T extends FielderConfigValue<T>>{

	private final String persistentString;

	public FielderConfigKey(String persistentString){
		this.persistentString = persistentString;
	}

	@Override
	public int hashCode(){
		return Objects.hash(persistentString);
	}

	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof FielderConfigKey)){
			return false;
		}
		FielderConfigKey<?> other = (FielderConfigKey<?>)obj;
		return Objects.equals(persistentString, other.persistentString);
	}
}
