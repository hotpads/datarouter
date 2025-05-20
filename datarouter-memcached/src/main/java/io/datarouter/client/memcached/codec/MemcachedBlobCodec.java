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

import java.util.function.Supplier;

import io.datarouter.client.memcached.util.MemcachedPathbeanResult;
import io.datarouter.client.memcached.util.MemcachedResult;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.util.Subpath;

public class MemcachedBlobCodec{

	private final Supplier<Subpath> rootPathSupplier;

	public MemcachedBlobCodec(Supplier<Subpath> rootPath){
		this.rootPathSupplier = rootPath;
	}

	public String encodeKey(PathbeanKey pk){
		return rootPathSupplier.get() + pk.getPathAndFile();
	}

	public PathbeanKey decodeKey(String stringKey){
		int rootPathLength = rootPathSupplier.get().toString().length();
		String stringPk = stringKey.substring(rootPathLength);
		return PathbeanKey.of(stringPk);
	}

	public MemcachedPathbeanResult decodeResult(MemcachedResult<byte[]> result){
		return new MemcachedPathbeanResult(PathbeanKey.of(result.key()), result.value());
	}

}
