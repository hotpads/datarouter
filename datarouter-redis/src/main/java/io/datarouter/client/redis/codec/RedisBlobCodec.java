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
package io.datarouter.client.redis.codec;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.util.Subpath;
import io.lettuce.core.KeyValue;

public class RedisBlobCodec{

	private final Supplier<byte[]> pathBytesSupplier;

	public RedisBlobCodec(Supplier<Subpath> pathSupplier){
		pathBytesSupplier = () -> StringCodec.UTF_8.encode(pathSupplier.get().toString());
	}

	public byte[] encodeKey(PathbeanKey pk){
		byte[] pkBytes = StringCodec.UTF_8.encode(pk.getPathAndFile());
		return ByteTool.concat(pathBytesSupplier.get(), pkBytes);
	}

	public byte[][] encodeKeys(List<PathbeanKey> pks){
		byte[][] encodedKeys = new byte[pks.size()][];
		for(int i = 0; i < pks.size(); ++i){
			encodedKeys[i] = encodeKey(pks.get(i));
		}
		return encodedKeys;
	}

	public PathbeanKey decodeKey(byte[] fullBytesKey){
		int offset = pathBytesSupplier.get().length;
		int length = fullBytesKey.length - offset;
		String pkString = StringCodec.UTF_8.decode(fullBytesKey, offset, length);
		return PathbeanKey.of(pkString);
	}

	public PathbeanKey decodeKey(KeyValue<byte[],byte[]> kv){
		return Optional.of(kv)
				.map(KeyValue::getKey)
				.map(this::decodeKey)
				.orElseThrow();
	}

}
