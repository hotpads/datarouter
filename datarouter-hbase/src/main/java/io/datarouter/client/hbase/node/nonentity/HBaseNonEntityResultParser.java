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
package io.datarouter.client.hbase.node.nonentity;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.client.hbase.HBaseClientManager;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.entity.EntityPartitioner;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.serialize.fieldcache.DatabeanFieldInfo;
import io.datarouter.util.array.ArrayTool;
import io.datarouter.util.lang.ReflectionTool;

public class HBaseNonEntityResultParser<
		EK extends EntityKey<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{

	private final EntityPartitioner<EK> partitioner;
	private final DatabeanFieldInfo<PK,D,F> fieldInfo;

	public HBaseNonEntityResultParser(
			EntityPartitioner<EK> partitioner,
			DatabeanFieldInfo<PK,D,F> fieldInfo){
		this.partitioner = partitioner;
		this.fieldInfo = fieldInfo;
	}

	public byte[] rowWithoutPrefix(byte[] rowWithPrefix){
		int offset = partitioner.getNumPrefixBytes();
		int length = rowWithPrefix.length - offset;
		return ByteTool.copyOfRange(rowWithPrefix, offset, length);
	}

	public PK toPk(byte[] rowWithPrefix){
		byte[] rowWithoutPrefix = rowWithoutPrefix(rowWithPrefix);
		PK primaryKey = fieldInfo.getPrimaryKeySupplier().get();
		if(ArrayTool.isEmpty(rowWithoutPrefix)){
			return primaryKey;
		}
		int byteOffset = 0;
		for(Field<?> field : fieldInfo.getPrimaryKeyFields()){
			int numBytesWithSeparator = field.numKeyBytesWithSeparator(rowWithoutPrefix, byteOffset);
			Object value = field.fromKeyBytesWithSeparatorButDoNotSet(rowWithoutPrefix, byteOffset);
			field.setUsingReflection(primaryKey, value);
			byteOffset += numBytesWithSeparator;
		}

		return primaryKey;
	}

	public PK toPk(Result result){
		return toPk(result.getRow());
	}

	public D toDatabean(Result result){
		PK pk = toPk(result);
		D databean = fieldInfo.getDatabeanSupplier().get();
		ReflectionTool.set(fieldInfo.getKeyJavaField(), databean, pk);
		while(result.advance()){
			Cell cell = result.current();
			String qualifier = StringCodec.UTF_8.decode(CellUtil.cloneQualifier(cell));
			if(HBaseClientManager.DUMMY_COL_NAME.equals(qualifier)){
				continue;
			}
			Field<?> field = fieldInfo.getFieldForColumnName(qualifier);
			if(field == null){
				continue;
			}
			Object value = field.fromValueBytesButDoNotSet(CellUtil.cloneValue(cell), 0);
			field.setUsingReflection(databean, value);
		}
		return databean;
	}

}
