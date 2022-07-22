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
import java.util.List;

import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.sql.MysqlSql;
import io.datarouter.client.mysql.sql.MysqlSqlFactory;
import io.datarouter.client.mysql.util.MysqlTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.file.DatabaseBlob;
import io.datarouter.storage.file.DatabaseBlob.DatabaseBlobFielder;
import io.datarouter.storage.file.DatabaseBlobKey;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.storage.util.Subpath;
import io.datarouter.util.string.StringTool;

public class MysqlLikePathOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseMysqlOp<List<DatabaseBlob>>{

	private static final List<String> COLUMNS_FOR_OP = List.of(
			DatabaseBlobKey.FieldKeys.pathAndFile.getColumnName(),
			DatabaseBlob.FieldKeys.size.getColumnName());

	private final MysqlSqlFactory mysqlSqlFactory;
	private final MysqlFieldCodecFactory fieldCodecFactory;
	private final PhysicalDatabeanFieldInfo<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> fieldInfo;
	private final Subpath path;
	private final String startKey;
	private final Config config;
	private final long nowMs;

	public MysqlLikePathOp(
			Datarouter datarouter,
			MysqlSqlFactory mysqlSqlFactory,
			MysqlFieldCodecFactory fieldCodecFactory,
			PhysicalDatabeanFieldInfo<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> fieldInfo,
			Subpath path,
			String startKey,
			Config config,
			long nowMs){
		super(datarouter, fieldInfo.getClientId());
		this.mysqlSqlFactory = mysqlSqlFactory;
		this.fieldCodecFactory = fieldCodecFactory;
		this.fieldInfo = fieldInfo;
		this.path = path;
		this.startKey = startKey;
		this.config = config;
		this.nowMs = nowMs;
	}

	@Override
	public List<DatabaseBlob> runOnce(){
		var sql = buildBlobSql(getClientId(),
				fieldInfo,
				COLUMNS_FOR_OP,
				startKey,
				path,
				mysqlSqlFactory,
				nowMs,
				config);
		Connection connection = getConnection();
		PreparedStatement statement = sql.prepare(connection);
		try{
			statement.execute();
			Scanner<Field<?>> fields = Scanner.of(COLUMNS_FOR_OP)
					.map(fieldInfo::getFieldForColumnName);
			return MysqlTool.getDatabeansFromSelectResult(
					fieldCodecFactory,
					fieldInfo.getDatabeanSupplier(),
					fields.list(),
					statement);
		}catch(SQLException e){
			throw new DataAccessException(String.format("error with select table=%s selectStatement=%s exception=%s",
					fieldInfo.getTableName(), statement, e));
		}
	}

	public static MysqlSql buildBlobSql(ClientId clientId,
			PhysicalDatabeanFieldInfo<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> fieldInfo,
			List<String> columnsForOp,
			String startKey,
			Subpath path,
			MysqlSqlFactory mysqlSqlFactory,
			long nowMs,
			Config config){
		var sql = mysqlSqlFactory.createSql(clientId, fieldInfo.getTableName(), fieldInfo.getDisableIntroducer())
				.append("select ")
				.append(String.join(", ", columnsForOp))
				.append(" from ")
				.append(fieldInfo.getTableName())
				.append(" where ")
				.append(DatabaseBlobKey.FieldKeys.pathAndFile.getColumnName())
				.append(" like ")
				.append(StringTool.escapeString(path.toString() + "%"));
		if(startKey != null){
			sql.append(" and ")
					.append(DatabaseBlobKey.FieldKeys.pathAndFile.getColumnName())
					.append(" > ")
					.append(StringTool.escapeString(startKey));
		}
		sql.append(" and ")
				.append("(")
				.append(DatabaseBlob.FieldKeys.expirationMs.getColumnName())
				.append(" > ")
				.append("" + nowMs)
				.append(" or ")
				.append(DatabaseBlob.FieldKeys.expirationMs.getColumnName())
				.append(" is null")
				.append(")");
		sql.append(" limit " + config.findResponseBatchSize().orElse(100));
		return sql;
	}

}
