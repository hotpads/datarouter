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
package io.datarouter.storage.scratch;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.bytes.ByteReader;
import io.datarouter.bytes.ByteWriter;
import io.datarouter.bytes.Codec;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldTool;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.scratch.ScratchDatabeanCodec.ScratchDatabeanBytes;
import io.datarouter.storage.serialize.fieldcache.DatabeanFieldInfo;
import io.datarouter.storage.serialize.fieldcache.IndexEntryFieldInfo;
import io.datarouter.util.Require;
import io.datarouter.util.lang.ReflectionTool;

/**
 * Things that use this codec should expect that the serialization format can change between each run of the app.
 * It allows flexibility for improving this codec.
 * It allows for optimizations like omitting columnNames.
 */
public class ScratchDatabeanCodec<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
implements Codec<D,ScratchDatabeanBytes>{

	private final Supplier<PK> primaryKeySupplier;
	private final List<Field<?>> primaryKeyFields;
	private final java.lang.reflect.Field primaryKeyJavaField;
	private final Supplier<D> databeanSupplier;
	private final F fielder;
	private final List<? extends Field<?>> nonKeyFields;

	public ScratchDatabeanCodec(DatabeanFieldInfo<PK,D,F> fieldInfo){
		this.primaryKeySupplier = fieldInfo.getPrimaryKeySupplier();
		this.primaryKeyFields = fieldInfo.getPrimaryKeyFields();
		this.primaryKeyJavaField = fieldInfo.getKeyJavaField();
		this.databeanSupplier = fieldInfo.getDatabeanSupplier();
		this.fielder = fieldInfo.getSampleFielder();
		this.nonKeyFields = fieldInfo.getNonKeyFields();
	}

	public ScratchDatabeanCodec(IndexEntryFieldInfo<PK,D,F> fieldInfo){
		this.primaryKeySupplier = fieldInfo.getPrimaryKeySupplier();
		this.primaryKeyFields = fieldInfo.getPrimaryKeyFields();
		this.primaryKeyJavaField = fieldInfo.getKeyJavaField();
		this.databeanSupplier = fieldInfo.getDatabeanSupplier();
		this.fielder = fieldInfo.getSampleFielder();
		this.nonKeyFields = fieldInfo.getNonKeyFields();
	}

	@Override
	public ScratchDatabeanBytes encode(D databean){
		// key
		byte[] keyBytes = FieldTool.getConcatenatedValueBytesTerminated(databean.getKey().getFields());
		// value
		var valueWriter = new ByteWriter(256);
		fielder.getNonKeyFields(databean).forEach(field -> {
			byte[] valueBytes = field.getValueBytes();
			boolean isPresent = valueBytes != null;
			valueWriter.booleanByte(isPresent);
			if(isPresent){
				valueWriter.varBytes(valueBytes);
			}
		});
		byte[] valueBytes = valueWriter.concat();
		return new ScratchDatabeanBytes(keyBytes, valueBytes);
	}

	@Override
	public D decode(ScratchDatabeanBytes databeanBytes){
		// pk
		byte[] pkBytes = databeanBytes.key();
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
		var reader = new ByteReader(databeanBytes.value());
		for(int i = 0; i < nonKeyFields.size(); ++i){
			boolean isPresent = reader.booleanByte();
			if(isPresent){
				Field<?> field = nonKeyFields.get(i);
				byte[] valueBytes = reader.varBytes();
				Object value = field.fromValueBytesButDoNotSet(valueBytes, 0);
				field.setUsingReflection(databean, value);
			}
		}
		Require.isFalse(reader.hasMore());
		return databean;
	}

	public record ScratchDatabeanBytes(
			byte[] key,
			byte[] value){
	}

}
