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
package io.datarouter.model.serialize.codec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldSetTool;
import io.datarouter.model.field.FieldTool;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.bytes.ByteTool;
import io.datarouter.util.bytes.IntegerByteTool;

public class BinaryDatabeanCodec{

	private final int databeanSchemaVersion;
	private final boolean allowNulls;
	private final boolean terminateIntermediateString;
	private final boolean terminateFinalString;

	private BinaryDatabeanCodec(
			int databeanSchemaVersion,
			boolean allowNulls,
			boolean terminateIntermediateString,
			boolean terminateFinalString){
		this.databeanSchemaVersion = databeanSchemaVersion;
		this.allowNulls = allowNulls;
		this.terminateIntermediateString = terminateIntermediateString;
		this.terminateFinalString = terminateFinalString;
	}

	public byte[] encode(PrimaryKey<?> pk){
		byte[] key = FieldTool.getConcatenatedValueBytes(
				pk.getFields(),
				allowNulls,
				terminateIntermediateString,
				terminateFinalString);
		byte[] schemaVersion = IntegerByteTool.getRawBytes(databeanSchemaVersion);
		return ByteTool.concatenate(schemaVersion, key);
	}

	public List<byte[]> encodeMulti(Collection<? extends PrimaryKey<?>> pks){
		return Scanner.of(pks)
				.map(this::encode)
				.list();
	}

	public <D> D decode(Supplier<D> supplier, Map<String,Field<?>> fieldByPrefixedName, byte[] bytes) throws Exception{
		return FieldSetTool.fieldSetFromBytes(supplier, fieldByPrefixedName, bytes);
	}

	public <D> List<D> decodeMulti(Supplier<D> supplier, Map<String,Field<?>> fieldByPrefixedName, List<byte[]> bytes)
	throws Exception{
		List<D> databeans = new ArrayList<>();
		for(byte[] byteArray : bytes){
			D databean = decode(supplier, fieldByPrefixedName, byteArray);
			databeans.add(databean);
		}
		return databeans;
	}

	public static class BinaryDatabeanCodecBuilder{

		private int databeanVersion = 1;
		private boolean allowNulls = false;
		private boolean terminateIntermediateString = false;
		private boolean terminateFinalString = false;

		public BinaryDatabeanCodecBuilder setDatabeanVersion(int databeanVersion){
			this.databeanVersion = databeanVersion;
			return this;
		}

		public BinaryDatabeanCodecBuilder setAllowNulls(boolean allowNulls){
			this.allowNulls = allowNulls;
			return this;
		}

		public BinaryDatabeanCodecBuilder setTerminateIntermediateString(boolean terminateIntermediateString){
			this.terminateIntermediateString = terminateIntermediateString;
			return this;
		}

		public BinaryDatabeanCodecBuilder setTerminateFinalString(boolean terminateFinalString){
			this.terminateFinalString = terminateFinalString;
			return this;
		}

		public BinaryDatabeanCodec build(){
			return new BinaryDatabeanCodec(
					databeanVersion,
					allowNulls,
					terminateIntermediateString,
					terminateFinalString);
		}

	}

}
