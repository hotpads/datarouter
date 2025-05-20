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

public class SpannerLikeKeyOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends SpannerBaseOp<List<DatabaseBlobKey>>{

	private static final String PATH_FILE_COLUMN = DatabaseBlobKey.FieldKeys.pathAndFile.getColumnName();

	private final PhysicalDatabeanFieldInfo<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder> fieldInfo;
	private final DatabaseClient client;
	private final Subpath path;
	private final Config config;
	private final String startKey;
	private final long nowMs;

	public SpannerLikeKeyOp(
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
	public List<DatabaseBlobKey> wrappedCall(){
		var sql = SpannerLikeOp.makeLikeSql(
				fieldInfo,
				List.of(PATH_FILE_COLUMN),
				startKey,
				path,
				nowMs,
				config);
		Statement statement = Statement.of(sql);
		return execute(statement);
	}

	private List<DatabaseBlobKey> execute(Statement statement){
		QueryOption[] options = {};
		ResultSet result = client.singleUse().executeQuery(statement, options);
		return Scanner.generate(result::next)
				.advanceWhile(hasNext -> hasNext)
				.map(_ -> new DatabaseBlobKey(
						PathbeanKey.of(result.getString(DatabaseBlobKey.FieldKeys.pathAndFile.getColumnName()))))
				.list();
	}

}
