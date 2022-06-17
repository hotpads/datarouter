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
package io.datarouter.gcp.spanner.sql;

import com.google.cloud.spanner.Statement;

import io.datarouter.gcp.spanner.field.SpannerBaseFieldCodec;
import io.datarouter.gcp.spanner.field.SpannerFieldCodecs;
import io.datarouter.model.field.Field;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.sql.Sql;
import io.datarouter.util.Require;

public class SpannerSql extends Sql<Void,Statement.Builder,SpannerSql>{

	private final SpannerFieldCodecs fieldCodecs;

	public SpannerSql(SpannerFieldCodecs fieldCodecs){
		super(SpannerSql.class);
		this.fieldCodecs = fieldCodecs;
	}

	@Override
	public SpannerSql appendColumnEqualsValueParameter(Field<?> field){
		SpannerBaseFieldCodec<?,?> codec = fieldCodecs.createCodec(field);
		int index = parameterSetters.size();
		appendParameter(codec.getParameterName(index, false), codec::setParameterValue);
		return this;
	}

	@Override
	public SpannerSql addSqlNameValueWithOperator(
			Field<?> field,
			String operator,
			boolean rejectNulls){
		if(rejectNulls && field.getValue() == null){
			throw new RuntimeException(field.getKey().getColumnName() + " should not be null, current sql is "
					+ sqlBuilder);
		}
		append(field.getKey().getColumnName());
		append(operator);
		int index = parameterSetters.size();
		SpannerBaseFieldCodec<?,?> codec = fieldCodecs.createCodec(field);
		appendParameter(codec.getParameterName(index, true), codec::setParameterValue);
		return this;
	}

	@Override
	public Statement.Builder prepare(Void unused){
		Statement.Builder statementBuilder = Statement.newBuilder(sqlBuilder.toString());
		for(int i = 0; i < parameterSetters.size(); ++i){
			parameterSetters.get(i).accept(statementBuilder, i);
		}
		return statementBuilder;
	}

	@Override
	public SpannerSql addLimitOffsetClause(Config config){
		if(config.findLimit().isPresent()){
			append(" limit " + config.getLimit());
		}
		if(config.findOffset().isPresent()){
			Require.isTrue(config.findLimit().isPresent(), "cannot use offset without limit");
			append(" offset " + config.getOffset());
		}
		return this;
	}

}