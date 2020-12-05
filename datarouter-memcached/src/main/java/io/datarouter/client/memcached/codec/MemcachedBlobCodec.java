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
package io.datarouter.client.memcached.codec;

import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.util.tuple.Pair;

public class MemcachedBlobCodec{

	private final String nodeName;
	private final int schemaVersion;

	public MemcachedBlobCodec(String nodeName, int nodeVersion){
		this.nodeName = nodeName;
		this.schemaVersion = nodeVersion;
	}

	public String encodeKey(PathbeanKey pk){
		return MemcachedKey.encode(nodeName, schemaVersion, pk);
	}

	public Pair<PathbeanKey,byte[]> decodeResult(Pair<String,Object> result){
		PathbeanKey key = MemcachedKey.decode(result.getLeft(), PathbeanKey.class).primaryKey;
		byte[] value = (byte[])result.getRight();
		return new Pair<>(key, value);
	}

}
