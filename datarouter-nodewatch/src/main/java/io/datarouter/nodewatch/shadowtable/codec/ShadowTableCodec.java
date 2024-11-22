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
package io.datarouter.nodewatch.shadowtable.codec;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import io.datarouter.bytes.ByteReader;
import io.datarouter.bytes.ByteWriter;
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.blockfile.row.BlockfileRow;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldTool;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.serialize.fieldcache.DatabeanFieldInfo;
import io.datarouter.util.lang.ReflectionTool;

public class ShadowTableCodec<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
implements Codec<D,BlockfileRow>{

	private final Supplier<PK> primaryKeySupplier;
	private final List<Field<?>> primaryKeyFields;
	private final java.lang.reflect.Field primaryKeyJavaField;
	private final Supplier<D> databeanSupplier;
	private final F fielder;
	private final Map<String,Field<?>> fieldByColumnName;

	public ShadowTableCodec(DatabeanFieldInfo<PK,D,F> fieldInfo){
		this.primaryKeySupplier = fieldInfo.getPrimaryKeySupplier();
		this.primaryKeyFields = fieldInfo.getPrimaryKeyFields();
		this.primaryKeyJavaField = fieldInfo.getKeyJavaField();
		this.databeanSupplier = fieldInfo.getDatabeanSupplier();
		this.fielder = fieldInfo.getSampleFielder();
		this.fieldByColumnName = fieldInfo.getFieldByColumnName();
	}

	@Override
	public BlockfileRow encode(D databean){
		// key
		byte[] keyBytes = FieldTool.getConcatenatedValueBytesTerminated(databean.getKey().getFields());
		// version
		long version = System.currentTimeMillis();
		// value
		var valueWriter = new ByteWriter(256);
		fielder.getNonKeyFields(databean).forEach(field -> {
			byte[] valueBytes = field.getValueBytes();
			if(valueBytes != null){
				valueWriter.varUtf8(field.getKey().getColumnName());
				valueWriter.varBytes(valueBytes);
			}
		});
		byte[] valueBytes = valueWriter.concat();
		return BlockfileRow.putWithLongVersion(
				keyBytes,
				version,
				valueBytes);
	}

	@Override
	public D decode(BlockfileRow blockfileRow){
		// pk
		byte[] pkBytes = blockfileRow.copyOfKey();
		PK pk = primaryKeySupplier.get();
		int cursor = 0;
		for(Field<?> field : primaryKeyFields){
			int numBytesWithSeparator = field.numKeyBytesWithSeparator(pkBytes, cursor);
			Object value = field.fromEscapedAndTerminatedKeyBytes(pkBytes, cursor);
			field.setUsingReflection(pk, value);
			cursor += numBytesWithSeparator;
		}
		// databean
		D databean = databeanSupplier.get();
		ReflectionTool.set(primaryKeyJavaField, databean, pk);
		var reader = new ByteReader(blockfileRow.copyOfValue());
		while(reader.hasMore()){
			String columnName = reader.varUtf8();
			Field<?> field = fieldByColumnName.get(columnName);
			byte[] valueBytes = reader.varBytes();
			if(field != null){// in case the field disappeared from the databean since the data was exported
				Object value = field.fromValueBytesButDoNotSet(valueBytes, 0);
				field.setUsingReflection(databean, value);
			}
		}
		return databean;
	}

	public long approximateDatabeanValueBytes(List<D> databeans){
		return databeans.stream()
				.mapToLong(this::approximateDatabeanValueBytes)
				.sum();
	}

	public long approximateDatabeanValueBytes(D databean){
		return fielder.getFields(databean).stream()
				.mapToLong(Field::getApproximateValueBytesLength)
				.sum();
	}

}
