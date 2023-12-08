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
package io.datarouter.storage.node.op.raw;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import io.datarouter.bytes.Codec;
import io.datarouter.bytes.Codec.NullPassthroughCodec;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.file.PathbeanKey;

public class EncodedBlobStorage<T>{

	private final BlobStorage blobStorage;
	private final Codec<T,byte[]> codec;

	public EncodedBlobStorage(BlobStorage blobStorage, Codec<T,byte[]> codec){
		this.blobStorage = blobStorage;
		this.codec = NullPassthroughCodec.of(codec);
	}

	/*-------- write ----------*/

	public void write(PathbeanKey key, T item){
		write(key, item, new Config());
	}

	public void write(PathbeanKey key, T item, Config config){
		blobStorage.write(key, codec.encode(item), config);
	}

	/*-------- read ----------*/

	@Deprecated// use find
	public T read(PathbeanKey key, Config config){
		return codec.decode(blobStorage.read(key, config).orElse(null));
	}

	public Optional<T> find(PathbeanKey key){
		return find(key, new Config());
	}

	public Optional<T> find(PathbeanKey key, Config config){
		return Optional.ofNullable(read(key, config));
	}

	public Map<PathbeanKey,T> readMulti(List<PathbeanKey> keys){
		return readMulti(keys, new Config());
	}

	public Map<PathbeanKey,T> readMulti(List<PathbeanKey> keys, Config config){
		return Scanner.of(blobStorage.readMulti(keys, config).entrySet())
				.toMap(Entry::getKey, entry -> codec.decode(entry.getValue()));
	}

}
