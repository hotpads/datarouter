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
package io.datarouter.client.memcached.client;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.util.PrimaryKeyPercentCodec;

public class MemcachedEncodedKey{

	public static final Integer DATAROUTER_VERSION = 3;

	public final String nodeName;
	public final Integer databeanVersion;
	public final PrimaryKey<?> primaryKey;

	public MemcachedEncodedKey(String nodeName, Integer databeanVersion, PrimaryKey<?> primaryKey){
		if(nodeName.contains(":")){
			throw new IllegalArgumentException("nodeName cannot contain \":\"");
		}
		this.nodeName = nodeName;
		this.databeanVersion = databeanVersion;
		this.primaryKey = primaryKey;
	}

	public String getVersionedKeyString(){
		String encodedPk = PrimaryKeyPercentCodec.encode(primaryKey);
		return DATAROUTER_VERSION + ":" + nodeName + ":" + databeanVersion + ":" + encodedPk;
	}

	public static List<String> getVersionedKeyStrings(
			String nodeName,
			int version,
			Collection<? extends PrimaryKey<?>> pks){
		return pks.stream()
				.filter(Objects::nonNull)
				.map(pk -> new MemcachedEncodedKey(nodeName, version, pk))
				.map(MemcachedEncodedKey::getVersionedKeyString)
				.collect(Collectors.toList());
	}

	public static <PK extends PrimaryKey<PK>> MemcachedEncodedKey parse(String string, Class<PK> pkClass){
		StringBuilder current = new StringBuilder();
		String[] parts = new String[3];
		int counter = 0;
		for(int i = 0; i < string.length(); i++){
			char character = string.charAt(i);
			if(character == ':'){
				parts[counter++] = current.toString();
				current = new StringBuilder();
			}else{
				current.append(character);
			}
		}
		if(counter != 3){
			throw new RuntimeException("incorrect number of parts, counter=" + counter + " input=" + string);
		}
		int databeanVersion = Integer.parseInt(parts[2]);
		PK primaryKey = PrimaryKeyPercentCodec.decode(pkClass, current.toString());
		return new MemcachedEncodedKey(parts[1], databeanVersion, primaryKey);
	}

}
