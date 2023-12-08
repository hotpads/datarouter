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
package io.datarouter.storage.setting;

import java.util.Objects;

public class DatarouterSettingTag{

	private final String persistentString;

	public DatarouterSettingTag(String persistentString){
		this.persistentString = persistentString;
	}

	public String getPersistentString(){
		return persistentString;
	}

	@Override
	public int hashCode(){
		return Objects.hash(persistentString);
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(obj == null){
			return false;
		}
		if(getClass() != obj.getClass()){
			return false;
		}
		DatarouterSettingTag other = (DatarouterSettingTag)obj;
		return Objects.equals(persistentString, other.persistentString);
	}

	@Override
	public String toString(){
		return "SettingTag [persistentString=" + persistentString + "]";
	}
}
