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
package io.datarouter.client.mysql.op.read.index;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.op.Isolation;
import io.datarouter.client.mysql.op.read.MysqlGetOpExecutor;
import io.datarouter.client.mysql.util.MysqlTool;
import io.datarouter.instrumentation.trace.TraceSpanFinisher;
import io.datarouter.instrumentation.trace.TracerThreadLocal;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.model.index.IndexEntry;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.IndexEntryFieldInfo;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;

public class MysqlGetIndexOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK,IE,PK,D>,
		IF extends DatabeanFielder<IK,IE>>
extends BaseMysqlOp<List<IE>>{

	private final MysqlGetOpExecutor mysqlGetOpExecutor;
	private final Config config;
	private final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;
	private final MysqlFieldCodecFactory fieldCodecFactory;
	private final Collection<IK> uniqueKeys;
	private final String opName;
	private final IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo;
	private final DatabeanFielder<IK,IE> indexFielder;
	private final Supplier<IE> indexEntrySupplier;
	private final IE indexEntry;

	public MysqlGetIndexOp(Datarouter datarouter, MysqlGetOpExecutor mysqlGetOpExecutor,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo, MysqlFieldCodecFactory fieldCodecFactory, String opName,
			Config config, IndexEntryFieldInfo<IK,IE,IF> indexEntryFieldInfo, Collection<IK> uniqueKeys){
		super(datarouter, fieldInfo.getClientId(), Isolation.DEFAULT, true);
		this.mysqlGetOpExecutor = mysqlGetOpExecutor;
		this.fieldInfo = fieldInfo;
		this.fieldCodecFactory = fieldCodecFactory;
		this.opName = opName;
		this.config = config;
		this.uniqueKeys = uniqueKeys;
		this.indexEntryFieldInfo = indexEntryFieldInfo;
		this.indexFielder = indexEntryFieldInfo.getFielderSupplier().get();
		this.indexEntrySupplier = indexEntryFieldInfo.getDatabeanSupplier();
		this.indexEntry = indexEntrySupplier.get();
	}

	@Override
	public List<IE> runOnce(){
		return mysqlGetOpExecutor.execute(fieldInfo, opName, uniqueKeys, config, indexFielder.getFields(
				indexEntry), this::select, getConnection(), indexEntryFieldInfo.getIndexName());
	}

	private List<IE> select(PreparedStatement ps){
		try{
			String spanName = fieldInfo.getNodeName() + " " + opName + " PreparedStatement.execute";
			try(TraceSpanFinisher finisher = TracerTool.startSpan(TracerThreadLocal.get(), spanName)){
				ps.execute();
			}
			ResultSet rs = ps.getResultSet();
			List<IE> databeans = new ArrayList<>();
			while(rs.next()){
				IE databean = MysqlTool.fieldSetFromMysqlResultSetUsingReflection(fieldCodecFactory, indexEntrySupplier,
						indexFielder.getFields(indexEntry), rs);
				databeans.add(databean);
			}
			return databeans;
		}catch(Exception e){
			String message = "error executing sql:" + ps;
			throw new DataAccessException(message, e);
		}
	}

}