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
package io.datarouter.plugin.dataexport.service;

import java.util.Map;
import java.util.function.Supplier;

import io.datarouter.bytes.ByteReader;
import io.datarouter.bytes.ByteWriter;
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.EmptyArray;
import io.datarouter.bytes.kvfile.KvFileEntry;
import io.datarouter.bytes.kvfile.KvFileOp;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.serialize.fieldcache.DatabeanFieldInfo;

public class DatabeanExportCodec<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
implements Codec<D,KvFileEntry>{

	private final Supplier<D> databeanSupplier;
	private final F fielder;
	private final Map<String,Field<?>> fieldByColumnName;

	public DatabeanExportCodec(DatabeanFieldInfo<PK,D,F> fieldInfo){
		this.databeanSupplier = fieldInfo.getDatabeanSupplier();
		this.fielder = fieldInfo.getSampleFielder();
		this.fieldByColumnName = fieldInfo.getFieldByColumnName();
	}

	@Override
	public KvFileEntry encode(D databean){
		var writer = new ByteWriter(256);
		Scanner.of(fielder.getFields(databean))
				.exclude(field -> field.getValue() == null)
				.forEach(field -> {
					writer.varUtf8(field.getKey().getColumnName());
					writer.varBytes(field.getValueBytes());
				});
		return KvFileEntry.create(
				writer.concat(),//put all the data in the KvFileEntry key since it's unsortable anyway
				EmptyArray.BYTE,
				KvFileOp.PUT,
				EmptyArray.BYTE);
	}

	@Override
	public D decode(KvFileEntry kvFileEntry){
		D fieldSet = databeanSupplier.get();
		var reader = new ByteReader(kvFileEntry.copyOfKey());
		while(reader.hasMore()){
			String columnName = reader.varUtf8();
			Field<?> field = fieldByColumnName.get(columnName);
			byte[] valueBytes = reader.varBytes();
			if(field != null){// in case the field disappeared from the databean since the data was exported
				Object value = field.fromValueBytesButDoNotSet(valueBytes, 0);
				field.setUsingReflection(fieldSet, value);
			}
		}
		return fieldSet;
	}

}
