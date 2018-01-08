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
package io.datarouter.storage.client;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.collection.ListTool;

public class ClientId implements Comparable<ClientId>{

	private final String name;
	private final boolean writable;
	/**
	 * If a client is disableable, it will need to check some setting to know if it's enabled.  This can lead to an
	 * infinite loop in the client which holds that setting.  Avoid the loop by setting disableable=false.
	 *
	 * Usually the client holding the ClusterSetting node is the only one requiring disableable=false;
	 */
	private final boolean disableable;

	public ClientId(String name, boolean writable){
		this(name, writable, true);
	}

	public ClientId(String name, boolean writable, boolean disableable){
		this.name = name;
		this.writable = writable;
		this.disableable = disableable;
	}

	@Override
	public int hashCode(){
		return Objects.hash(name, writable, disableable);
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(!(obj instanceof ClientId)){
			return false;
		}
		ClientId other = (ClientId)obj;
		if(!name.equals(other.name)){
			return false;
		}
		if(writable != other.writable){
			return false;
		}
		if(disableable != other.disableable){
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(ClientId other){
		int diff = name.compareTo(other.name);
		if(diff != 0){
			return diff;
		}
		diff = Boolean.compare(writable, other.writable);
		if(diff != 0){
			return diff;
		}
		return Boolean.compare(disableable, disableable);
	}

	@Override
	public String toString(){
		return "ClientId[" + name + "," + writable + "," + disableable + "]";
	}

	public static List<String> getNames(Collection<ClientId> ids){
		List<String> names = ListTool.createArrayListWithSize(ids);
		for(ClientId id : CollectionTool.nullSafe(ids)){
			names.add(id.name);
		}
		return names;
	}

	public static List<String> getWritableNames(Collection<ClientId> ids){
		List<String> names = ListTool.createArrayListWithSize(ids);
		for(ClientId id : CollectionTool.nullSafe(ids)){
			if(id.writable){
				names.add(id.name);
			}
		}
		return names;
	}

	public String getName(){
		return name;
	}

	public boolean getWritable(){
		return writable;
	}

	public boolean getDisableable(){
		return disableable;
	}

}
