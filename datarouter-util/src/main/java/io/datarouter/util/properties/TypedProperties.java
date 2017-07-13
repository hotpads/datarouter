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
package io.datarouter.util.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import io.datarouter.util.BooleanTool;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.string.StringTool;

public class TypedProperties{

	private List<Properties> propertiesList;

	public TypedProperties(Collection<Properties> propertiesList){
		this.propertiesList = new ArrayList<>(propertiesList);
	}

	public TypedProperties(String path){
		Properties properties = PropertiesTool.parse(path);
		if(properties != null){
			this.propertiesList = ListTool.wrap(properties);
		}
	}


	/********************* defaultable ******************************/

	public String getString(String key, String def){
		String val = getString(key);
		return val == null ? def : val;
	}

	public String getString(String key){
		return PropertiesTool.getFirstOccurrence(propertiesList, key);
	}

	public Optional<String> optString(String key){
		return Optional.ofNullable(getString(key));
	}

	/*************** typed **********************/

	public Boolean getBoolean(String key, boolean def){
		Boolean val = getBoolean(key);
		return val == null ? def : val;
	}

	public Boolean getBoolean(String key){
		String val = getString(key);
		if(StringTool.isEmpty(val)){
			return null;
		}
		return BooleanTool.isTrue(val);
	}

	public Integer getInteger(String key, int def){
		Integer val = getInteger(key);
		return val == null ? def : val;
	}

	public Integer getInteger(String key){
		String val = getString(key);
		if(StringTool.isEmpty(val)){
			return null;
		}
		return Integer.valueOf(val);
	}

	public Short getShort(String key){
		String val = getString(key);
		if(StringTool.isEmpty(val)){
			return null;
		}
		return Short.valueOf(val);
	}

	public Long getLong(String key){
		String val = getString(key);
		if(StringTool.isEmpty(val)){
			return null;
		}
		return Long.valueOf(val);
	}

	public Float getFloat(String key){
		String val = getString(key);
		if(StringTool.isEmpty(val)){
			return null;
		}
		return Float.valueOf(val);
	}

	public Double getDouble(String key){
		String val = getString(key);
		if(StringTool.isEmpty(val)){
			return null;
		}
		return Double.valueOf(val);
	}

	/***************** required **********************************/

	public String getRequiredString(String key){
		String str = getString(key);
		if(str == null){
			throw new IllegalArgumentException("cannot find required String " + key);
		}
		return str;
	}

	/****************** basic ************************************/

	public List<Properties> getUnmodifiablePropertiesList(){
		return Collections.unmodifiableList(propertiesList);
	}
}
