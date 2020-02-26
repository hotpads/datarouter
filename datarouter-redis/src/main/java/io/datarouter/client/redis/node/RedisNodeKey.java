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
package io.datarouter.client.redis.node;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.util.PrimaryKeyPercentCodec;

public class RedisNodeKey{

	private static final Integer VERSION = 1;

	private final String nodeName;
	private final Integer databeanVersion;
	private final PrimaryKey<?> primaryKey;

	public RedisNodeKey(String nodeName, Integer databeanVersion, PrimaryKey<?> primaryKey){
		if(nodeName.contains(":")){
			throw new IllegalArgumentException("nodeName cannot contain \":\"");
		}
		this.nodeName = nodeName;
		this.databeanVersion = databeanVersion;
		this.primaryKey = primaryKey;
	}

	public String getVersionedKeyString(){
		String encodedPk = PrimaryKeyPercentCodec.encode(primaryKey);
		return VERSION + ":" + nodeName + ":" + databeanVersion + ":" + encodedPk;
	}

	public static List<String> getVersionedKeyStrings(String nodeName, int version,
			Collection<? extends PrimaryKey<?>> keys){
		return keys.stream()
				.filter(Objects::nonNull)
				.map(pk -> new RedisNodeKey(nodeName, version, pk))
				.map(RedisNodeKey::getVersionedKeyString)
				.collect(Collectors.toList());
	}

}
