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

import java.util.Arrays;
import java.util.Optional;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.IntegerByteTool;
import io.datarouter.model.databean.DatabeanTool;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldTool;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.storage.tally.Tally;
import io.datarouter.storage.tally.Tally.TallyFielder;
import io.datarouter.storage.tally.TallyKey;
import io.datarouter.util.array.ArrayTool;
import io.lettuce.core.KeyValue;

public class RedisTallyCodec{

	private static final TallyFielder SAMPLE_FIELDER = new TallyFielder();

	private final int version;
	private final PhysicalDatabeanFieldInfo<TallyKey,Tally,TallyFielder> fieldInfo;

	@SuppressWarnings("unchecked")
	public RedisTallyCodec(int version, PhysicalDatabeanFieldInfo<?,?,?> fieldInfo){
		this.version = version;
		this.fieldInfo = (PhysicalDatabeanFieldInfo<TallyKey,Tally,TallyFielder>)fieldInfo;
	}

	public byte[] encodeKey(TallyKey pk){
		byte[] key = FieldTool.getConcatenatedValueBytes(pk.getFields());
		byte[] schemaVersion = IntegerByteTool.getRawBytes(version);
		return ByteTool.concatenate(schemaVersion, key);
	}

	public byte[] encode(Tally databean){
		return DatabeanTool.getBytes(databean, SAMPLE_FIELDER);
	}

	public TallyKey decodeKey(byte[] row){
		// first 4 bytes are schema version
		byte[] bytes = Arrays.copyOfRange(row, 4, row.length);
		TallyKey primaryKey = fieldInfo.getPrimaryKeySupplier().get();
		if(ArrayTool.isEmpty(row)){
			return primaryKey;
		}
		int byteOffset = 0;
		for(Field<?> field : fieldInfo.getPrimaryKeyFields()){
			int numBytesWithSeparator = field.numBytesWithSeparator(bytes, byteOffset);
			Object value = field.fromBytesWithSeparatorButDoNotSet(bytes, byteOffset);
			field.setUsingReflection(primaryKey, value);
			byteOffset += numBytesWithSeparator;
		}
		return primaryKey;
	}

	public Optional<Long> decodeTallyValue(Optional<byte[]> byteTally){
		if(byteTally.isEmpty() || byteTally.get().length == 0){
			return Optional.empty();
		}
		// returned byte is ascii value of the long
		return byteTally
				.map(String::new)
				.map(String::trim)
				.map(Long::valueOf);
	}

	public Optional<Long> decodeTallyValue(KeyValue<byte[],byte[]> entry){
		if(!entry.hasValue()){
			return Optional.empty();
		}
		return decodeTallyValue(Optional.of(entry.getValue()));
	}

}
