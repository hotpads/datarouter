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
package io.datarouter.client.mysql.op.write;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;

import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.op.Isolation;
import io.datarouter.client.mysql.sql.MysqlSqlFactory;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.exception.DataAccessException;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.array.ByteArrayField;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.file.DatabaseBlob;
import io.datarouter.storage.file.DatabaseBlob.DatabaseBlobFielder;
import io.datarouter.storage.file.DatabaseBlobKey;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;

public class MysqlPutBlobOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseMysqlOp<Void>{

	private final PathbeanKey key;
	private final byte[] value;
	private final MysqlSqlFactory mysqlSqlFactory;
	private final PhysicalDatabeanFieldInfo<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> fieldInfo;
	private final Config config;

	public MysqlPutBlobOp(
			DatarouterClients datarouterClients,
			PhysicalDatabeanFieldInfo<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> fieldInfo,
			MysqlSqlFactory mysqlSqlFactory,
			PathbeanKey key,
			byte[] value,
			Config config){
		super(datarouterClients, fieldInfo.getClientId(), Isolation.readCommitted, true);
		this.mysqlSqlFactory = mysqlSqlFactory;
		this.fieldInfo = fieldInfo;
		this.key = key;
		this.value = value;
		this.config = config;
	}

	@Override
	public Void runOnce(){
		Long currentMillis = System.currentTimeMillis();
		Long expiresAt = config.findTtl()
				.map(Duration::toMillis)
				.map(ttlMs -> currentMillis + ttlMs)
				.orElse(null);
		Connection connection = getConnection();
		DatabaseBlob blob = new DatabaseBlob(key, value, expiresAt);
		List<Field<?>> fields = fieldInfo.getFieldsWithValues(blob);
		ByteArrayField byteField = new ByteArrayField(DatabaseBlob.FieldKeys.data, value);
		var putBlobSql = mysqlSqlFactory
				.createSql(getClientId(), fieldInfo.getTableName(), fieldInfo.getDisableIntroducer())
				.insert(fieldInfo.getTableName(), List.of(fields), isAutoCommit())
				.append(" on duplicate key update ")
				.append(DatabaseBlob.FieldKeys.size.getColumnName())
				.append(" = ")
				.append("" + blob.getSize())
				.append(", ")
				.appendSqlNameValue(byteField, false)
				.append(" , ")
				.append(DatabaseBlob.FieldKeys.expirationMs.getColumnName())
				.append(" = ")
				.append("IF(")
				.append(DatabaseBlob.FieldKeys.expirationMs.getColumnName())
				.append("<")
				.append("" + currentMillis)
				.append(", " + expiresAt)
				.append(", " + DatabaseBlob.FieldKeys.expirationMs.getColumnName())
				.append(")");
		PreparedStatement statement = putBlobSql.prepare(connection);
		try{
			statement.execute();
		}catch(SQLException e){
			throw new DataAccessException(String.format("error with update table=%s selectStatement=%s exception=%s",
					fieldInfo.getTableName(), statement, e));
		}
		return null;
	}

}
