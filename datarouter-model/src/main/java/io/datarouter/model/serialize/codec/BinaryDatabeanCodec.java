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
package io.datarouter.model.serialize.codec;

import java.util.Map;
import java.util.function.Supplier;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.codec.intcodec.RawIntCodec;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldSetTool;
import io.datarouter.model.field.FieldTool;
import io.datarouter.model.key.primary.PrimaryKey;

public class BinaryDatabeanCodec{

	private static final RawIntCodec RAW_INT_CODEC = RawIntCodec.INSTANCE;

	private final int databeanSchemaVersion;

	private BinaryDatabeanCodec(int databeanSchemaVersion){
		this.databeanSchemaVersion = databeanSchemaVersion;
	}

	public byte[] encode(PrimaryKey<?> pk){
		byte[] key = FieldTool.getConcatenatedValueBytes(pk.getFields());
		byte[] schemaVersion = RAW_INT_CODEC.encode(databeanSchemaVersion);
		return ByteTool.concatenate2(schemaVersion, key);
	}

	public <D> D decode(Supplier<D> supplier, Map<String,Field<?>> fieldByPrefixedName, byte[] bytes){
		return FieldSetTool.fieldSetFromBytes(supplier, fieldByPrefixedName, bytes);
	}

	public static class BinaryDatabeanCodecBuilder{

		private int databeanVersion = 1;

		public BinaryDatabeanCodecBuilder setDatabeanVersion(int databeanVersion){
			this.databeanVersion = databeanVersion;
			return this;
		}

		public BinaryDatabeanCodec build(){
			return new BinaryDatabeanCodec(databeanVersion);
		}

	}

}
