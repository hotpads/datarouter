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


import java.util.Collection;
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
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.storage.tally.Tally;
import io.datarouter.storage.tally.TallyKey;
import io.datarouter.util.string.StringTool;

public class SpannerFindTallyOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends SpannerBaseOp<List<Tally>>{

	private final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;
	private final DatabaseClient client;
	private final Collection<String> keys;
	private final Config config;

	public SpannerFindTallyOp(DatabaseClient client,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<String> keys,
			Config config){
		super("SpannerFind: " + fieldInfo.getTableName());
		this.client = client;
		this.fieldInfo = fieldInfo;
		this.keys = keys;
		this.config = config;
	}

	@Override
	public List<Tally> wrappedCall(){
		long nowMs = System.currentTimeMillis();
		return Scanner.of(keys)
			.batch(config.findRequestBatchSize().orElse(100))
			.map(batch -> buildQuery(batch, nowMs))
			.map(Statement::of)
			.concatIter(this::execute)
			.list();
	}

	private List<Tally> execute(Statement statement){
		QueryOption[] options = {};
		ResultSet result = client.singleUse().executeQuery(statement, options);
		return Scanner.generate(result::next)
				.advanceWhile(hasNext -> hasNext)
				.map($ -> new Tally(
					result.getString(TallyKey.FieldKeys.id.getColumnName()), result.getLong(
					Tally.FieldKeys.tally.getColumnName())))
				.list();
	}

	private String buildQuery(List<String> keys, long nowMs){
		var query = new StringBuilder();
		query.append("select * ");
		query.append(" from " + fieldInfo.getTableName());
		query.append(" where ");
		query.append("(");
		boolean didOne = false;
		for(String key : keys){
			if(didOne){
				query.append(" or ");
			}
			query.append(TallyKey.FieldKeys.id.getColumnName() + " = " + StringTool.escapeString(key));
			didOne = true;
		}
		query.append(")");
		query.append(" and ");
		query.append("(");
		query.append(Tally.FieldKeys.expirationMs.getColumnName() + " is null ");
		query.append(" or ");
		query.append(Tally.FieldKeys.expirationMs.getColumnName() + " > " + nowMs);
		query.append(")");
		return query.toString();
	}

}
