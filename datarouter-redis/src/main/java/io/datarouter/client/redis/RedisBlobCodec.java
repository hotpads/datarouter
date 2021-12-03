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
package io.datarouter.client.redis;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.IntegerByteTool;
import io.datarouter.model.field.FieldTool;
import io.datarouter.storage.file.PathbeanKey;

public class RedisBlobCodec{

	private static final int CODEC_VERSION = 1;

	private final int schemaVersion;

	public RedisBlobCodec(int nodeVersion){
		this.schemaVersion = nodeVersion;
	}

	public byte[] encodeKey(PathbeanKey pk){
		byte[] codecVersion = IntegerByteTool.getRawBytes(CODEC_VERSION);
		byte[] key = FieldTool.getConcatenatedValueBytes(pk.getFields());
		byte[] version = IntegerByteTool.getRawBytes(schemaVersion);
		return ByteTool.concatenate(codecVersion, version, key);
	}

}
