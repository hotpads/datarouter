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
package io.datarouter.plugin.dataexport.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import io.datarouter.bytes.BinaryDictionary;
import io.datarouter.bytes.ByteReader;
import io.datarouter.bytes.ByteWriter;
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.blockfile.row.BlockfileRow;
import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldTool;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.serialize.fieldcache.DatabeanFieldInfo;
import io.datarouter.util.lang.ReflectionTool;

public class DatabeanExportCodec<
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
	private final Codec<String,Integer> columnNameCodec;

	public DatabeanExportCodec(DatabeanFieldInfo<PK,D,F> fieldInfo, ColumnNameCodec columnNameCodec){
		this.primaryKeySupplier = fieldInfo.getPrimaryKeySupplier();
		this.primaryKeyFields = fieldInfo.getPrimaryKeyFields();
		this.primaryKeyJavaField = fieldInfo.getKeyJavaField();
		this.databeanSupplier = fieldInfo.getDatabeanSupplier();
		this.fielder = fieldInfo.getSampleFielder();
		this.fieldByColumnName = fieldInfo.getFieldByColumnName();
		this.columnNameCodec = columnNameCodec;
	}

	@Override
	public BlockfileRow encode(D databean){
		// key
		byte[] keyBytes = FieldTool.getConcatenatedValueBytesTerminated(databean.getKey().getFields());
		// version
		long version = System.currentTimeMillis();
		// value
		var writer = new ByteWriter(256);
		fielder.getNonKeyFields(databean).forEach(field -> {
			byte[] valueBytes = field.getValueBytes();
			if(valueBytes != null){
					int columnId = columnNameCodec.encode(field.getKey().getColumnName());
					writer.varInt(columnId);
					writer.varBytes(field.getValueBytes());
			}
		});
		byte[] valueBytes = writer.concat();
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
			int columnId = reader.varInt();
			String columnName = columnNameCodec.decode(columnId);
			Field<?> field = fieldByColumnName.get(columnName);
			byte[] valueBytes = reader.varBytes();
			if(field != null){// in case the field disappeared from the databean since the data was exported
				Object value = field.fromValueBytesButDoNotSet(valueBytes, 0);
				field.setUsingReflection(databean, value);
			}
		}
		return databean;
	}


	public static class ColumnNameCodec implements Codec<String,Integer>{
		// PK bytes are concatenated, but this includes their column names anyway, in case they come in handy elsewhere.
		private final List<String> columnNames;
		private final Map<String,Integer> idByColumnName;

		private ColumnNameCodec(List<String> columnNames){
			this.columnNames = columnNames;
			idByColumnName = new HashMap<>();
			for(int i = 0; i < columnNames.size(); ++i){
				idByColumnName.put(columnNames.get(i), i);
			}
		}

		public static ColumnNameCodec createNewMappings(DatabeanFieldInfo<?,?,?> fieldInfo){
			return new ColumnNameCodec(fieldInfo.getFieldColumnNames());
		}

		public static ColumnNameCodec fromBinaryDictionary(BinaryDictionary dictionary){
			List<String> columnNames = ColumnNamesDictionaryCodec.getFromBinaryDictionary(dictionary);
			return new ColumnNameCodec(columnNames);
		}

		@Override
		public Integer encode(String value){
			return idByColumnName.get(value);
		}

		@Override
		public String decode(Integer id){
			return columnNames.get(id);
		}

	}

	public static class ColumnNamesDictionaryCodec implements Codec<List<String>,byte[]>{

		public static final ColumnNamesDictionaryCodec INSTANCE = new ColumnNamesDictionaryCodec();

		private static final String DICTIONARY_KEY = "COLUMN_NAMES";

		public static void addToDictionary(List<String> columnNames, BinaryDictionary dictionary){
			dictionary.put(
					StringCodec.UTF_8.encode(DICTIONARY_KEY),
					INSTANCE.encode(columnNames));
		}

		public static List<String> getFromBinaryDictionary(BinaryDictionary dictionary){
			return dictionary.find(StringCodec.UTF_8.encode(DICTIONARY_KEY))
					.map(INSTANCE::decode)
					.orElseThrow();
		}

		@Override
		public byte[] encode(List<String> columnNames){
			var writer = new ByteWriter(128);
			columnNames.forEach(writer::varUtf8);
			return writer.concat();
		}

		@Override
		public List<String> decode(byte[] bytes){
			var reader = new ByteReader(bytes);
			List<String> columnNames = new ArrayList<>();
			while(reader.hasMore()){
				columnNames.add(reader.varUtf8());
			}
			return columnNames;
		}

	}

}
