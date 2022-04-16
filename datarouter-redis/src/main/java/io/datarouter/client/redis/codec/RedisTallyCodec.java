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
import io.datarouter.storage.util.Subpath;
import io.lettuce.core.KeyValue;

public class RedisTallyCodec{

	private final byte[] pathBytes;

	public RedisTallyCodec(Subpath path){
		pathBytes = StringCodec.UTF_8.encode(path.toString());
	}

	public byte[] encodeId(String id){
		byte[] idBytes = StringCodec.UTF_8.encode(id);
		return ByteTool.concat(pathBytes, idBytes);
	}

	public String decodeId(byte[] bytes){
		int offset = pathBytes.length;
		int length = bytes.length - offset;
		return StringCodec.UTF_8.decode(bytes, offset, length);
	}

	public String decodeId(KeyValue<byte[],byte[]> kv){
		return Optional.of(kv)
				.map(KeyValue::getKey)
				.map(this::decodeId)
				.orElseThrow();
	}

	public long decodeValue(KeyValue<byte[],byte[]> kv){
		return Optional.of(kv)
				.filter(KeyValue::hasValue)
				.map(KeyValue::getValue)
				.map(this::decodeValue)
				.orElseThrow();
	}

	/**
	 * @param bytes String value of a long returned by Lettuce
	 */
	public long decodeValue(byte[] bytes){
		return Optional.of(bytes)
				.map(StringCodec.US_ASCII::decode)
				.map(String::trim)
				.map(Long::valueOf)
				.orElseThrow();
	}

}
