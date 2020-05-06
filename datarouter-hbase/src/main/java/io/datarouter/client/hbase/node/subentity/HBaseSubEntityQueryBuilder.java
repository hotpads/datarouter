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
package io.datarouter.client.hbase.node.subentity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.filter.ColumnPrefixFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.KeyOnlyFilter;

import io.datarouter.client.hbase.node.entity.HBaseEntityQueryBuilder;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.field.FieldTool;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.serialize.fieldcache.EntityFieldInfo;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.util.bytes.ByteTool;
import io.datarouter.util.bytes.StringByteTool;

public class HBaseSubEntityQueryBuilder<
		EK extends EntityKey<EK>,
		E extends Entity<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends HBaseEntityQueryBuilder<EK,E>{

	public static final int BATCH_SIZE = 100;

	private final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;

	public HBaseSubEntityQueryBuilder(
			EntityFieldInfo<EK,E> entityFieldInfo,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo){
		super(entityFieldInfo);
		this.fieldInfo = fieldInfo;
	}

	/*---------------------------- qualifiers -------------------------------*/

	public byte[] getQualifier(PK primaryKey, String fieldName){
		return ByteTool.concatenate(
				fieldInfo.getEntityColumnPrefixBytes(),
				getQualifierPkBytes(primaryKey, true),
				StringByteTool.getUtf8Bytes(fieldName));
	}

	private byte[] getQualifierPrefix(PK primaryKey){
		return ByteTool.concatenate(fieldInfo.getEntityColumnPrefixBytes(), getQualifierPkBytes(primaryKey, true));
	}

	public byte[] getQualifierPkBytes(PK primaryKey, boolean trailingSeparatorAfterEndingString){
		if(primaryKey == null){
			return new byte[]{};
		}
		return FieldTool.getConcatenatedValueBytes(
				primaryKey.getPostEntityKeyFields(),
				true,
				trailingSeparatorAfterEndingString,
				trailingSeparatorAfterEndingString);
	}

	/*---------------------------- get/getMulti -----------------------------*/

	public List<Get> getGets(Collection<PK> pks, boolean keysOnly){
		List<Get> gets = new ArrayList<>(pks.size());
		for(PK pk : pks){
			byte[] rowBytes = getRowBytesWithPartition(pk.getEntityKey());
			byte[] qualifierPrefix = getQualifierPrefix(pk);
			Get get = new Get(rowBytes);
			if(keysOnly){
				FilterList filters = new FilterList();
				filters.addFilter(new KeyOnlyFilter());
				filters.addFilter(new ColumnPrefixFilter(qualifierPrefix));
				get.setFilter(filters);
			}else{
				get.setFilter(new ColumnPrefixFilter(qualifierPrefix));
			}
			gets.add(get);
		}
		return gets;
	}

}
