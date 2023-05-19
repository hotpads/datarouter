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
package io.datarouter.client.mysql.op.read;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.op.Isolation;
import io.datarouter.client.mysql.sql.MysqlSqlFactory;
import io.datarouter.client.mysql.util.MysqlTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.file.DatabaseBlob;
import io.datarouter.storage.file.DatabaseBlob.DatabaseBlobFielder;
import io.datarouter.storage.file.DatabaseBlobKey;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;

public class MysqlGetBlobOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseMysqlOp<List<DatabaseBlob>>{

	private final Collection<PathbeanKey> keys;
	private final MysqlSqlFactory mysqlSqlFactory;
	private final PhysicalDatabeanFieldInfo<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> fieldInfo;
	private final MysqlFieldCodecFactory fieldCodecFactory;
	private final Config config;
	private final List<String> fields;

	public MysqlGetBlobOp(
			DatarouterClients datarouterClients,
			PhysicalDatabeanFieldInfo<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> fieldInfo,
			MysqlSqlFactory mysqlSqlFactory,
			MysqlFieldCodecFactory fieldCodecFactory,
			Collection<PathbeanKey> keys,
			List<String> fields,
			Config config){
		super(datarouterClients, fieldInfo.getClientId(), Isolation.readCommitted, true);
		this.keys = keys;
		this.mysqlSqlFactory = mysqlSqlFactory;
		this.fieldInfo = fieldInfo;
		this.fieldCodecFactory = fieldCodecFactory;
		this.config = config;
		this.fields = fields;
	}

	@Override
	public List<DatabaseBlob> runOnce(){
		long nowMs = System.currentTimeMillis();
		return Scanner.of(keys)
				.map(PathbeanKey::getPathAndFile)
				.batch(config.findRequestBatchSize().orElse(100))
				.concatIter(batch -> execute(batch, fields, nowMs))
				.list();
	}

	private List<DatabaseBlob> execute(
			List<String> keys,
			List<String> fields,
			long nowMs){
		List<DatabaseBlobKey> blobKeys = Scanner.of(keys)
				.map(PathbeanKey::of)
				.map(DatabaseBlobKey::new)
				.list();
		Scanner<Field<?>> fieldsForQuery = Scanner.of(fields)
				.map(field -> fieldInfo.getFieldForColumnName(field));
		List<Field<?>> fieldsForQueryList = fieldsForQuery.list();
		var selectSql = mysqlSqlFactory
				.createSql(getClientId(), fieldInfo.getTableName(), fieldInfo.getDisableIntroducer())
				.addSelectFromClause(fieldInfo.getTableName(), fieldsForQueryList)
				.appendWhereClauseDisjunctionClosed(blobKeys)
				.append(" and ")
				.append("(")
				.append(DatabaseBlob.FieldKeys.expirationMs.getColumnName())
				.append(" > ")
				.append("" + nowMs)
				.append(" or ")
				.append(DatabaseBlob.FieldKeys.expirationMs.getColumnName())
				.append(" is null")
				.append(")");
		Connection connection = getConnection();
		PreparedStatement selectStatement = selectSql.prepare(connection);
		try{
			selectStatement.execute();
			return MysqlTool.getDatabeansFromSelectResult(
					fieldCodecFactory,
					fieldInfo.getDatabeanSupplier(),
					fieldsForQueryList,
					selectStatement);
		}catch(SQLException e){
			throw new DataAccessException(String.format("error with select table=%s selectStatement=%s exception=%s",
					fieldInfo.getTableName(), selectStatement, e));
		}
	}

}
