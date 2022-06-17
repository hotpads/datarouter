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
package io.datarouter.aws.memcached.client;

import io.datarouter.enums.MappedEnum;
import io.datarouter.storage.config.client.MemcachedGenericClientOptions.MemcachedGenericClientMode;
import net.spy.memcached.ClientMode;

/**
 * Wrapper to not expose memcached client libraries to application code
 */
public enum MemcachedClientMode{
	STATIC(ClientMode.Static, "static"),
	DYNAMIC(ClientMode.Dynamic, "dynamic");

	public static final MappedEnum<MemcachedClientMode,String> BY_PERSISTENT_STRING
			= new MappedEnum<>(values(), value -> value.persistentString);

	public final ClientMode clientMode;
	public final String persistentString;

	MemcachedClientMode(ClientMode clientMode, String persistentString){
		this.clientMode = clientMode;
		this.persistentString = persistentString;
	}

	public static MemcachedClientMode fromGenericClientMode(MemcachedGenericClientMode genericMode){
		return switch(genericMode){
			case STATIC -> STATIC;
			case DYNAMIC -> DYNAMIC;
		};
	}

}
