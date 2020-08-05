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
package io.datarouter.aws.memcached.client;

import java.util.Optional;

import io.datarouter.util.enums.DatarouterEnumTool;
import io.datarouter.util.enums.StringEnum;
import net.spy.memcached.ClientMode;

/**
 * Wrapper to not expose memcached client libraries to application code
 */
public enum MemcachedClientMode implements StringEnum<MemcachedClientMode>{
	STATIC(ClientMode.Static, "static"),
	DYNAMIC(ClientMode.Dynamic, "dynamic"),
	;

	private final ClientMode clientMode;
	private final String persistentString;

	MemcachedClientMode(ClientMode clientMode, String persistentString){
		this.clientMode = clientMode;
		this.persistentString = persistentString;
	}

	public ClientMode getClientMode(){
		return clientMode;
	}

	@Override
	public String getPersistentString(){
		return persistentString;
	}

	@Override
	public MemcachedClientMode fromPersistentString(String string){
		return fromPersistentStringStatic(string)
				.orElseThrow();
	}

	public static Optional<MemcachedClientMode> fromPersistentStringStatic(String string){
		return DatarouterEnumTool.getEnumFromStringOptional(values(), string);
	}

}
