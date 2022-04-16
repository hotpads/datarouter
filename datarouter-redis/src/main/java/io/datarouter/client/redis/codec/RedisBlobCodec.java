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

import java.util.Optional;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.util.Subpath;
import io.lettuce.core.KeyValue;

public class RedisBlobCodec{

	private final byte[] pathBytes;

	public RedisBlobCodec(Subpath path){
		pathBytes = StringCodec.UTF_8.encode(path.toString());
	}

	public byte[] encodeKey(PathbeanKey pk){
		byte[] pkBytes = StringCodec.UTF_8.encode(pk.getPathAndFile());
		return ByteTool.concat(pathBytes, pkBytes);
	}

	public PathbeanKey decodeKey(byte[] fullBytesKey){
		int offset = pathBytes.length;
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
