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
package io.datarouter.util.properties;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import io.datarouter.util.BooleanTool;
import io.datarouter.util.string.StringTool;

public class TypedProperties{

	private final List<Properties> propertiesList;

	public TypedProperties(){
		this.propertiesList = new ArrayList<>();
	}

	public TypedProperties(Collection<Properties> propertiesList){
		this.propertiesList = new ArrayList<>(propertiesList);
	}

	public TypedProperties(String path){
		this();
		Properties properties = PropertiesTool.parse(path);
		if(properties != null){
			this.propertiesList.add(properties);
		}
	}

	public void addProperties(Properties properties){
		propertiesList.add(properties);
	}

	/*------------------------- defaultable ---------------------------------*/

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

	/*------------------------- typed ---------------------------------------*/

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

	public Optional<InetSocketAddress> optInetSocketAddress(String hostnameAndPort){
		String val = getString(hostnameAndPort);
		if(StringTool.isEmpty(val)){
			return Optional.empty();
		}
		String[] hostnameAndPortTokens = val.split(":");
		String hostname = hostnameAndPortTokens[0];
		int port = Integer.parseInt(hostnameAndPortTokens[1]);
		return Optional.of(new InetSocketAddress(hostname, port));
	}

	/*------------------------- required ------------------------------------*/

	public String getRequiredString(String key){
		return Objects.requireNonNull(getString(key), "cannot find required String " + key);
	}

	public int getRequiredInteger(String key){
		return Objects.requireNonNull(getInteger(key), "cannot find required Integer " + key);
	}

	/*------------------------- basic ---------------------------------------*/

	public List<Properties> getUnmodifiablePropertiesList(){
		return Collections.unmodifiableList(propertiesList);
	}

}
