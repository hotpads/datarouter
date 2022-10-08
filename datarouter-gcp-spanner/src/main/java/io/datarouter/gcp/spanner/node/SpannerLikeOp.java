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
package io.datarouter.gcp.spanner.node;

import java.util.List;

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
import io.datarouter.storage.file.DatabaseBlob.DatabaseBlobFielder;
import io.datarouter.storage.file.DatabaseBlobKey;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.storage.util.Subpath;
import io.datarouter.util.string.StringTool;

public class SpannerLikeOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends SpannerBaseOp<List<DatabaseBlob>>{

	private static final List<String> COLUMNS_FOR_OP = List.of(
			DatabaseBlobKey.FieldKeys.pathAndFile.getColumnName(),
			DatabaseBlob.FieldKeys.size.getColumnName());

	private final PhysicalDatabeanFieldInfo<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> fieldInfo;
	private final DatabaseClient client;
	private final Subpath path;
	private final Config config;
	private final String startKey;
	private final long nowMs;

	public SpannerLikeOp(
			DatabaseClient client,
			PhysicalDatabeanFieldInfo<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> fieldInfo,
			String startKey,
			Config config,
			Subpath path,
			long nowMs){
		super("SpannerLike: " + fieldInfo.getTableName());
		this.fieldInfo = fieldInfo;
		this.client = client;
		this.startKey = startKey;
		this.config = config;
		this.path = path;
		this.nowMs = nowMs;
	}

	@Override
	public List<DatabaseBlob> wrappedCall(){
		var sql = makeLikeSql(
				fieldInfo,
				COLUMNS_FOR_OP,
				startKey,
				path,
				nowMs,
				config);
		Statement statement = Statement.of(sql);
		return execute(statement);
	}

	private List<DatabaseBlob> execute(Statement statement){
		QueryOption[] options = {};
		ResultSet result = client.singleUse().executeQuery(statement, options);
		return Scanner.generate(result::next)
				.advanceWhile(hasNext -> hasNext)
				.map($ -> new DatabaseBlob(
						new DatabaseBlobKey(
								PathbeanKey.of(result.getString(
										DatabaseBlobKey.FieldKeys.pathAndFile.getColumnName()))),
						result.getLong(DatabaseBlob.FieldKeys.size.getColumnName())))
				.list();
	}

	public static String makeLikeSql(
			PhysicalDatabeanFieldInfo<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> fieldInfo,
			List<String> columnsForOp,
			String startKey,
			Subpath path,
			long nowMs,
			Config config){
		var builder = new StringBuilder();
		builder.append("select ");
		builder.append(String.join(", ", columnsForOp));
		builder.append(" from ")
				.append(fieldInfo.getTableName())
				.append(" where ")
				.append(DatabaseBlobKey.FieldKeys.pathAndFile.getColumnName())
				.append(" like ")
				.append(StringTool.escapeString(path.toString() + "%"));
		if(startKey != null){
			builder.append(" and ")
					.append(DatabaseBlobKey.FieldKeys.pathAndFile.getColumnName())
					.append(" > ")
					.append(StringTool.escapeString(startKey));
		}
		builder.append(" and ")
				.append("(")
				.append(DatabaseBlob.FieldKeys.expirationMs.getColumnName())
				.append(" > ")
				.append("" + nowMs)
				.append(" or ")
				.append(DatabaseBlob.FieldKeys.expirationMs.getColumnName())
				.append(" is null")
				.append(")");
		builder.append(" limit " + config.findResponseBatchSize().orElse(100));
		return builder.toString();
	}

}
