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
package io.datarouter.gcp.spanner.op.read;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.Options.QueryOption;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Statement;

import io.datarouter.gcp.spanner.op.SpannerBaseOp;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.file.DatabaseBlob;
import io.datarouter.storage.file.DatabaseBlobKey;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.util.string.StringTool;

public class SpannerGetBlobOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends SpannerBaseOp<List<DatabaseBlob>>{

	private static final String PATH_AND_FILE = DatabaseBlobKey.FieldKeys.pathAndFile.getColumnName();
	private static final String EXPIRATION_MS = DatabaseBlob.FieldKeys.expirationMs.getColumnName();
	private static final String SIZE = DatabaseBlob.FieldKeys.size.getColumnName();
	private static final String DATA = DatabaseBlob.FieldKeys.data.getColumnName();

	private final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;
	private final DatabaseClient client;
	private final Collection<PathbeanKey> keys;
	private final Config config;
	private final List<String> fields;

	public SpannerGetBlobOp(DatabaseClient client,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<PathbeanKey> keys,
			Config config,
			List<String> fields){
		super("SpannerGetBlob: " + fieldInfo.getTableName());
		this.client = client;
		this.fieldInfo = fieldInfo;
		this.keys = keys;
		this.config = config;
		this.fields = fields;
	}

	@Override
	public List<DatabaseBlob> wrappedCall(){
		long nowMs = System.currentTimeMillis();
		return Scanner.of(keys)
				.map(PathbeanKey::getPathAndFile)
				.batch(config.findRequestBatchSize().orElse(100))
				.map(batch -> buildQuery(fieldInfo.getTableName(), batch, fields, nowMs))
				.map(Statement::of)
				.concatIter(this::execute)
				.list();
	}

	private List<DatabaseBlob> execute(Statement statement){
		Set<String> fieldsForQuery = new HashSet<>(fields);
		QueryOption[] options = {};
		ResultSet resultSet = client.singleUse().executeQuery(statement, options);
		List<DatabaseBlob> resultDataBlobs = new ArrayList<>();
		while(resultSet.next()){
			DatabaseBlob blob = buildDatabaseBlob(resultSet, fieldsForQuery);
			resultDataBlobs.add(blob);
		}
		return resultDataBlobs;
	}

	private static DatabaseBlob buildDatabaseBlob(ResultSet result, Set<String> fieldsForQuery){
		Long size = fieldsForQuery.contains(SIZE)
				&& !result.isNull(SIZE)
				? result.getLong(SIZE)
				: null;
		String pathAndFile = fieldsForQuery.contains(PATH_AND_FILE)
				&& !result.isNull(PATH_AND_FILE)
				? result.getString(PATH_AND_FILE)
				: null;
		byte[] data = fieldsForQuery.contains(DATA)
				&& !result.isNull(DATA)
				? result.getBytes(DATA).toByteArray()
				: null;
		Long expirationMs = fieldsForQuery.contains(EXPIRATION_MS)
				&& !result.isNull(EXPIRATION_MS)
				? result.getLong(EXPIRATION_MS)
				: null;
		return new DatabaseBlob(
				PathbeanKey.of(pathAndFile),
				data,
				size,
				expirationMs);
	}

	private static String buildQuery(String tableName, List<String> keys, List<String> fields, long nowMs){
		var query = new StringBuilder();
		query.append("select  ");
		query.append(String.join(", ", fields));
		query.append(" from " + tableName);
		query.append(" where ");
		query.append("(");
		boolean didOne = false;
		for(String key : keys){
			if(didOne){
				query.append(" or ");
			}
			query.append(PATH_AND_FILE + " = " + StringTool.escapeString(key));
			didOne = true;
		}
		query.append(")");
		query.append(" and ");
		query.append("(");
		query.append(EXPIRATION_MS + " is null ");
		query.append(" or ");
		query.append(EXPIRATION_MS + " > " + nowMs);
		query.append(")");
		return query.toString();
	}

}
