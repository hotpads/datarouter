package com.hotpads.datarouter.client.imp.mysql.ddl.generate.imp;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.hotpads.datarouter.client.imp.mysql.ddl.domain.CharSequenceSqlColumn;
import com.hotpads.datarouter.client.imp.mysql.ddl.domain.MySqlCharacterSet;
import com.hotpads.datarouter.client.imp.mysql.ddl.domain.MySqlCollation;
import com.hotpads.datarouter.client.imp.mysql.ddl.domain.MySqlRowFormat;
import com.hotpads.datarouter.client.imp.mysql.ddl.domain.MySqlTableEngine;
import com.hotpads.datarouter.client.imp.mysql.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.mysql.ddl.domain.SqlIndex;
import com.hotpads.datarouter.client.imp.mysql.ddl.domain.SqlTable;
import com.hotpads.datarouter.client.imp.mysql.field.JdbcFieldCodec;
import com.hotpads.datarouter.client.imp.mysql.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.storage.field.Field;

public class FieldSqlTableGenerator{

	@Inject
	private JdbcFieldCodecFactory fieldCodecFactory;

	public SqlTable generate(String tableName, List<Field<?>> primaryKeyFields, List<Field<?>> nonKeyFields,
			MySqlCollation collation, MySqlCharacterSet characterSet, MySqlRowFormat rowFormat,
			Map<String,List<Field<?>>> indexes, Map<String,List<Field<?>>> uniqueIndexes){

		List<SqlColumn> primaryKeyColumns = makeSqlColumns(primaryKeyFields, false);
		List<SqlColumn> columns = makeSqlColumns(nonKeyFields, true);
		columns.addAll(primaryKeyColumns);

		SqlIndex primaryKey = SqlIndex.createPrimaryKey(primaryKeyColumns);

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
				rowFormat, MySqlTableEngine.INNODB);
	}

	private List<SqlColumn> makeSqlColumns(List<Field<?>> fields, boolean allowNullable){
		return fields.stream()
				.map((Function<Field<?>, JdbcFieldCodec<?,?>>)fieldCodecFactory::createCodec)
				.map(codec -> codec.getSqlColumnDefinition(allowNullable))
				.collect(Collectors.toList());
	}

	private Set<SqlIndex> makeSqlIndexes(Map<String,List<Field<?>>> indexes){
		return indexes.entrySet().stream()
				.map(entry -> new SqlIndex(entry.getKey(), makeSqlColumns(entry.getValue(), true)))
				.collect(Collectors.toSet());
	}

}
