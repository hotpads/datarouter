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
package io.datarouter.client.memory.util;

import io.datarouter.bytes.ByteReader;
import io.datarouter.bytes.ByteWriter;
import io.datarouter.bytes.Codec;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.serialize.fieldcache.DatabeanFieldInfo;

public class MemoryDatabeanCodec<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
implements Codec<D,byte[]>{

	private final DatabeanFieldInfo<PK,D,F> fieldInfo;

	public MemoryDatabeanCodec(DatabeanFieldInfo<PK,D,F> fieldInfo){
		this.fieldInfo = fieldInfo;
	}

	@Override
	public byte[] encode(D databean){
		var writer = new ByteWriter(32);
		Scanner.of(fieldInfo.getSampleFielder().getFields(databean))
				.exclude(field -> field.getValue() == null)
				.forEach(field -> {
					writer.varUtf8(field.getKey().getColumnName());
					writer.varBytes(field.getValueBytes());
				});
		return writer.concat();
	}

	@Override
	public D decode(byte[] bytes){
		D fieldSet = fieldInfo.getDatabeanSupplier().get();
		var reader = new ByteReader(bytes);
		while(reader.hasMore()){
			String columnName = reader.varUtf8();
			Field<?> field = fieldInfo.getFieldByColumnName().get(columnName);
			byte[] valueBytes = reader.varBytes();
			Object value = field.fromValueBytesButDoNotSet(valueBytes, 0);
			field.setUsingReflection(fieldSet, value);
		}
		return fieldSet;
	}

}
