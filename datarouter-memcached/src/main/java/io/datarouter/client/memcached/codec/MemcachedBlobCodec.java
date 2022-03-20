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
package io.datarouter.client.memcached.codec;

import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.util.Subpath;
import io.datarouter.util.tuple.Pair;

public class MemcachedBlobCodec{

	private final Subpath rootPath;
	private final int rootPathLength;

	public MemcachedBlobCodec(Subpath rootPath){
		this.rootPath = rootPath;
		rootPathLength = rootPath.toString().length();
	}

	public String encodeKey(PathbeanKey pk){
		return rootPath + pk.getPathAndFile();
	}

	public PathbeanKey decodeKey(String stringKey){
		String stringPk = stringKey.substring(rootPathLength);
		return PathbeanKey.of(stringPk);
	}

	public Pair<PathbeanKey,byte[]> decodeResult(Pair<String,Object> result){
		PathbeanKey key = PathbeanKey.of(result.getLeft());
		//TODO push the casting up to MemcachedOps
		byte[] valueBytes = (byte[])result.getRight();
		return new Pair<>(key, valueBytes);
	}

}
