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
package io.datarouter.gcp.bigtable.node;

import java.util.Map;

import com.google.cloud.bigtable.data.v2.models.Row;
import com.google.cloud.bigtable.data.v2.models.RowCell;
import com.google.protobuf.ByteString;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.serialize.fieldcache.DatabeanFieldInfo;
import io.datarouter.util.array.ArrayTool;
import io.datarouter.util.lang.ReflectionTool;

public class BigtableResultParser<
		EK extends EntityKey<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{

	private final DatabeanFieldInfo<PK,D,F> fieldInfo;

	public BigtableResultParser(
			DatabeanFieldInfo<PK,D,F> fieldInfo){
		this.fieldInfo = fieldInfo;
	}

	public PK toPk(byte[] row){
		PK primaryKey = fieldInfo.getPrimaryKeySupplier().get();
		if(ArrayTool.isEmpty(row)){
			return primaryKey;
		}
		int byteOffset = 0;
		for(Field<?> field : fieldInfo.getPrimaryKeyFields()){
			int numBytesWithSeparator = field.numKeyBytesWithSeparator(row, byteOffset);
			Object value = field.fromEscapedAndTerminatedKeyBytes(row, byteOffset);
			field.setUsingReflection(primaryKey, value);
			byteOffset += numBytesWithSeparator;
		}

		return primaryKey;
	}

	public PK toPk(Row row){
		return toPk(row.getKey().toByteArray());
	}

	public D toDatabean(Row row){
		PK pk = toPk(row);
		D databean = fieldInfo.getDatabeanSupplier().get();
		ReflectionTool.set(fieldInfo.getKeyJavaField(), databean, pk);
		Map<ByteString,ByteString> qualifierToValue = Scanner.of(row.getCells())
				.toMap(RowCell::getQualifier, RowCell::getValue);
		for(Field<?> field : fieldInfo.getNonKeyFields()){
			ByteString columnNameBytes = ByteString.copyFrom(field.getKey().getColumnNameBytes());
			ByteString result = qualifierToValue.get(columnNameBytes);
			if(result == null){
				continue;
			}
			Object value = field.fromValueBytesButDoNotSet(result.toByteArray(), 0);
			field.setUsingReflection(databean, value);
		}
		return databean;
	}

}
