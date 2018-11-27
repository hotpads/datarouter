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
package io.datarouter.client.mysql.util;

import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldSet;
import io.datarouter.model.field.FieldTool;
import io.datarouter.storage.config.Config;
import io.datarouter.util.collection.CollectionTool;

public class SqlBuilder{

	public static final String PRIMARY_KEY_INDEX_NAME = "PRIMARY";

	/*--------------------------- primary methods -------------------------- */

	public static String deleteAll(Config config, String tableName){
		StringBuilder sql = new StringBuilder();
		addDeleteFromClause(sql, tableName);
		addLimitOffsetClause(sql, config);
		return sql.toString();
	}

	/*-------------------------- secondary methods ------------------------- */

	public static void addForceIndexClause(StringBuilder sql, String indexName){
		sql.append(" force index (").append(indexName).append(")");
	}

	public static void addSelectFromClause(StringBuilder sql, String tableName, List<Field<?>> selectFields){
		sql.append("select ");
		FieldTool.appendCsvColumnNames(sql, selectFields);
		sql.append(" from " + tableName);
	}

	public static void addDeleteFromClause(StringBuilder sql, String tableName){
		sql.append("delete from " + tableName);
	}

	public static void addUpdateClause(StringBuilder sql, String tableName){
		sql.append("update ").append(tableName).append(" set ");
	}

	public static boolean needsRangeWhereClause(FieldSet<?> start, FieldSet<?> end){
		return start != null && FieldTool.countNonNullLeadingFields(start.getFields()) > 0
				|| end != null && FieldTool.countNonNullLeadingFields(end.getFields()) > 0;
	}

	public static void addOrderByClause(StringBuilder sql, List<Field<?>> orderByFields){
		if(CollectionTool.isEmpty(orderByFields)){
			return;
		}
		sql.append(" order by ");
		int counter = 0;
		for(Field<?> field : orderByFields){
			if(counter > 0){
				sql.append(", ");
			}
			sql.append(field.getKey().getColumnName() + " asc");
			++counter;
		}
	}

	public static void addLimitOffsetClause(StringBuilder sql, Config config){
		config = Config.nullSafe(config);

		if(config.getLimit() != null && config.getOffset() != null){
			sql.append(" limit " + config.getOffset() + ", " + config.getLimit());
		}else if(config.getLimit() != null){
			sql.append(" limit " + config.getLimit());
		}else if(config.getOffset() != null){
			sql.append(" limit " + config.getOffset() + ", " + Integer.MAX_VALUE);// stupid mysql syntax
		}
	}

}
