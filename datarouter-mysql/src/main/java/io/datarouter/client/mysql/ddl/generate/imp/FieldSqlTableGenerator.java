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
package io.datarouter.client.mysql.ddl.generate.imp;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.datarouter.client.mysql.ddl.domain.CharSequenceSqlColumn;
import io.datarouter.client.mysql.ddl.domain.MysqlCharacterSet;
import io.datarouter.client.mysql.ddl.domain.MysqlCollation;
import io.datarouter.client.mysql.ddl.domain.MysqlRowFormat;
import io.datarouter.client.mysql.ddl.domain.MysqlTableEngine;
import io.datarouter.client.mysql.ddl.domain.SqlColumn;
import io.datarouter.client.mysql.ddl.domain.SqlIndex;
import io.datarouter.client.mysql.ddl.domain.SqlTable;
import io.datarouter.client.mysql.field.MysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldKey;
import io.datarouter.util.iterable.IterableTool;

public class FieldSqlTableGenerator{

	@Inject
	private MysqlFieldCodecFactory fieldCodecFactory;

	public SqlTable generate(String tableName, List<Field<?>> primaryKeyFields, List<Field<?>> nonKeyFields,
			MysqlCollation collation, MysqlCharacterSet characterSet, MysqlRowFormat rowFormat,
			Map<String,List<Field<?>>> indexes, Map<String,List<Field<?>>> uniqueIndexes){

		List<SqlColumn> primaryKeyColumns = makeSqlColumns(primaryKeyFields, false);
		List<String> primaryKeyColumnNames = IterableTool.map(primaryKeyColumns, SqlColumn::getName);
		List<SqlColumn> columns = makeSqlColumns(nonKeyFields, true);
		columns.addAll(primaryKeyColumns);

		SqlIndex primaryKey = SqlIndex.createPrimaryKey(primaryKeyColumnNames);

		Set<SqlIndex> sqlIndexes = makeSqlIndexes(indexes);
		Set<SqlIndex> sqlUniqueIndexes = makeSqlIndexes(uniqueIndexes);

		//TODO set charset and collation elsewhere
		columns.stream()
				.filter(column -> column instanceof CharSequenceSqlColumn)
				.map(CharSequenceSqlColumn.class::cast)
				.forEach(column -> {
					column.setCharacterSet(characterSet);
					column.setCollation(collation);
				});

		return new SqlTable(tableName, primaryKey, columns, sqlIndexes, sqlUniqueIndexes, characterSet, collation,
				rowFormat, MysqlTableEngine.INNODB);
	}

	private List<SqlColumn> makeSqlColumns(List<Field<?>> fields, boolean allowNullable){
		return fields.stream()
				.map((Function<Field<?>, MysqlFieldCodec<?>>)fieldCodecFactory::createCodec)
				.map(codec -> codec.getSqlColumnDefinition(allowNullable))
				.collect(Collectors.toList());
	}

	private List<String> makeSqlColumnNames(List<Field<?>> fields){
		return fields.stream()
				.map(Field::getKey)
				.map(FieldKey::getColumnName)
				.collect(Collectors.toList());
	}

	private Set<SqlIndex> makeSqlIndexes(Map<String,List<Field<?>>> indexes){
		return indexes.entrySet().stream()
				.map(entry -> new SqlIndex(entry.getKey(), makeSqlColumnNames(entry.getValue())))
				.collect(Collectors.toSet());
	}

}
