package com.hotpads.datarouter.client.imp.jdbc.ddl.generate.imp;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCharacterSet;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCollation;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlRowFormat;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlTableEngine;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlIndex;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.datarouter.client.imp.jdbc.field.JdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.storage.field.Field;

public class FieldSqlTableGenerator{

	@Inject
	private JdbcFieldCodecFactory fieldCodecFactory;

	public SqlTable generate(String tableName, List<Field<?>> primaryKeyFields, List<Field<?>> nonKeyFields,
			MySqlCollation collation, MySqlCharacterSet characterSet, MySqlRowFormat rowFormat,
			Map<String,List<Field<?>>> indexes, Map<String,List<Field<?>>> uniqueIndexes){

		List<SqlColumn> primaryKeyColumns = makeSqlColumns(primaryKeyFields);
		List<SqlColumn> columns = makeSqlColumns(nonKeyFields);
		columns.addAll(primaryKeyColumns);

		SqlIndex primaryKey = new SqlIndex(tableName + " primary key", primaryKeyColumns);

		Set<SqlIndex> sqlIndexes = makeSqlIndexes(indexes);
		Set<SqlIndex> sqlUniqueIndexes = makeSqlIndexes(uniqueIndexes);

		//TODO pass the charset and collation to JdbcFieldCodec::getSqlColumnDefinition
		columns.forEach(column -> {
			column.setCharacterSet(characterSet);
			column.setCollation(collation);
		});

		return new SqlTable(tableName, primaryKey, columns, sqlIndexes, sqlUniqueIndexes, characterSet, collation,
				rowFormat, MySqlTableEngine.INNODB);
	}

	private List<SqlColumn> makeSqlColumns(List<Field<?>> fields){
		return fields.stream()
				.map((Function<Field<?>, JdbcFieldCodec<?,?>>)fieldCodecFactory::createCodec)
				.map(JdbcFieldCodec::getSqlColumnDefinition)
				.collect(Collectors.toList());
	}

	private Set<SqlIndex> makeSqlIndexes(Map<String,List<Field<?>>> indexes){
		return indexes.entrySet().stream()
				.map(entry -> new SqlIndex(entry.getKey(), makeSqlColumns(entry.getValue())))
				.collect(Collectors.toSet());
	}

}
