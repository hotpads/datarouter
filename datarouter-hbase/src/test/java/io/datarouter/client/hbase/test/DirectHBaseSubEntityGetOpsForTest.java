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
package io.datarouter.client.hbase.test;

import java.util.List;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Table;

import io.datarouter.client.hbase.HBaseClientManager;
import io.datarouter.client.hbase.node.subentity.HBaseSubEntityNode;
import io.datarouter.client.hbase.node.subentity.HBaseSubEntityQueryBuilder;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.serialize.fieldcache.EntityFieldInfo;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.util.mutable.MutableString;

/**
 * Helper for writing HBase entity integration tests
 */
public class DirectHBaseSubEntityGetOpsForTest<
		EK extends EntityKey<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>>{

	public final Table table;
	public final List<Get> hbaseGetOps;
	public final EntityFieldInfo<EK,?> entityFieldInfo;
	public final PhysicalDatabeanFieldInfo<PK,D,?> fieldInfo;

	public DirectHBaseSubEntityGetOpsForTest(
			HBaseClientManager hBaseClientManager,
			ClientId clientId,
			HBaseSubEntityNode<EK,?,PK,D,?> subEntityNode,
			PK primaryKey){
		this.entityFieldInfo = subEntityNode.getEntityFieldInfo();
		this.fieldInfo = subEntityNode.getFieldInfo();
		var queryBuilder = new HBaseSubEntityQueryBuilder<>(entityFieldInfo, fieldInfo);
		String tableName = fieldInfo.getTableName();
		MutableString progress = new MutableString("");
		this.table = hBaseClientManager.checkOutTable(clientId, tableName, progress);

		this.hbaseGetOps = queryBuilder.getGets(List.of(primaryKey), false);
	}

}
