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
package io.datarouter.storage.client;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import io.datarouter.scanner.Scanner;

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
	private final ClientId relatedWriterClient;

	private ClientId(String name, boolean writable, boolean disableable, ClientId relatedWriterClient){
		this.name = name;
		this.writable = writable;
		this.disableable = disableable;
		this.relatedWriterClient = relatedWriterClient;
	}

	public static ClientId writer(String name, boolean disableable){
		return new ClientId(name, true, disableable, null);
	}

	public static ClientId reader(String name, boolean disableable, ClientId relatedWriterClient){
		return new ClientId(name, false, disableable, Objects.requireNonNull(relatedWriterClient));
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
		if(!(obj instanceof ClientId other)){
			return false;
		}
		return Objects.equals(name, other.name)
				&& writable == other.writable
				&& disableable == other.disableable;
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
		return Scanner.of(ids)
				.map(clientId -> clientId.name)
				.list();
	}

	public static List<String> getWritableNames(Collection<ClientId> ids){
		return Scanner.of(ids)
				.include(clientId -> clientId.writable)
				.map(clientId -> clientId.name)
				.list();
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

	public Optional<ClientId> getRelatedWriterClient(){
		return Optional.ofNullable(relatedWriterClient);
	}

}
