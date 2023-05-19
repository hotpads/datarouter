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
import io.datarouter.client.mysql.sql.MysqlSqlFactory;
import io.datarouter.client.mysql.util.MysqlTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.file.DatabaseBlob;
import io.datarouter.storage.file.DatabaseBlob.DatabaseBlobFielder;
import io.datarouter.storage.file.DatabaseBlobKey;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.storage.util.Subpath;

public class MysqlLikePathKeyOp<
	PK extends PrimaryKey<PK>,
	D extends Databean<PK,D>,
	F extends DatabeanFielder<PK,D>>
extends BaseMysqlOp<List<DatabaseBlobKey>>{

	private static final String PATH_FILE_COLUMN = DatabaseBlobKey.FieldKeys.pathAndFile.getColumnName();

	private final MysqlSqlFactory mysqlSqlFactory;
	private final MysqlFieldCodecFactory fieldCodecFactory;
	private final PhysicalDatabeanFieldInfo<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> fieldInfo;
	private final Subpath path;
	private final String startKey;
	private final Config config;
	private final long nowMs;

	public MysqlLikePathKeyOp(
			DatarouterClients datarouterClients,
			MysqlSqlFactory mysqlSqlFactory,
			MysqlFieldCodecFactory fieldCodecFactory,
			PhysicalDatabeanFieldInfo<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> fieldInfo,
			Subpath path,
			String startKey,
			Config config,
			long nowMs){
		super(datarouterClients, fieldInfo.getClientId());
		this.mysqlSqlFactory = mysqlSqlFactory;
		this.fieldCodecFactory = fieldCodecFactory;
		this.fieldInfo = fieldInfo;
		this.path = path;
		this.startKey = startKey;
		this.config = config;
		this.nowMs = nowMs;
	}

	@Override
	public List<DatabaseBlobKey> runOnce(){
		var sql = MysqlLikePathOp.buildBlobSql(getClientId(),
				fieldInfo,
				List.of(PATH_FILE_COLUMN),
				startKey,
				path,
				mysqlSqlFactory,
				nowMs,
				config);
		Connection connection = getConnection();
		PreparedStatement statement = sql.prepare(connection);
		try{
			statement.execute();
			return MysqlTool.selectPrimaryKeys(
					fieldCodecFactory,
					fieldInfo,
					statement);
		}catch(SQLException e){
			throw new DataAccessException(String.format("error with select table=%s selectStatement=%s exception=%s",
					fieldInfo.getTableName(), statement, e));
		}
	}

}