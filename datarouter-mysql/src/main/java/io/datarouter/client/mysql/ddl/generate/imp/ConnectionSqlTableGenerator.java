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
package io.datarouter.client.mysql.ddl.generate.imp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.datarouter.client.mysql.connection.MysqlConnectionPoolHolder.MysqlConnectionPool;
import io.datarouter.client.mysql.ddl.domain.CharSequenceSqlColumn;
import io.datarouter.client.mysql.ddl.domain.MysqlCharacterSet;
import io.datarouter.client.mysql.ddl.domain.MysqlCollation;
import io.datarouter.client.mysql.ddl.domain.MysqlColumnType;
import io.datarouter.client.mysql.ddl.domain.MysqlRowFormat;
import io.datarouter.client.mysql.ddl.domain.MysqlTableEngine;
import io.datarouter.client.mysql.ddl.domain.SqlColumn;
import io.datarouter.client.mysql.ddl.domain.SqlIndex;
import io.datarouter.client.mysql.ddl.domain.SqlTable;
import io.datarouter.util.BooleanTool;

public class ConnectionSqlTableGenerator{

	private static final String INFORMATION_SCHEMA = "information_schema";
	private static final String ENGINE = "engine";
	private static final String ROW_FORMAT = "row_format";
	private static final String TABLE_COLLATION = "table_collation";

	private static final Pattern COLUMN_TYPE_PATTERN = Pattern.compile("\\d+");

	public static SqlTable generate(MysqlConnectionPool connectionPool, String tableName, String schemaName){
		try(Connection connection = connectionPool.checkOut()){
			List<SqlColumn> columns = fetchSqlTableColumns(connection, schemaName, tableName);
			SqlTableIndexes sqlTableIndexes = fetchSqlTableIndexes(connection, schemaName, tableName);
			SqlTableMetadata sqlTableMetadata = fetchSqlTableMetadata(connection, schemaName, tableName);
			return new SqlTable(
					tableName,
					sqlTableIndexes.primaryKey,
					columns,
					sqlTableIndexes.indexes,
					sqlTableIndexes.uniqueIndexes,
					sqlTableMetadata.characterSet,
					sqlTableMetadata.collation,
					sqlTableMetadata.rowFormat,
					sqlTableMetadata.engine);
		}catch(SQLException e){
			throw new RuntimeException("can not read schema information for table " + schemaName + "." + tableName, e);
		}
	}

	private static List<SqlColumn> fetchSqlTableColumns(Connection connection, String schemaName, String tableName)
	throws SQLException{
		PreparedStatement statement = connection.prepareStatement("select * from " + INFORMATION_SCHEMA + "."
				+ "columns where table_schema = ? and table_name = ?");
		statement.setString(1, schemaName);
		statement.setString(2, tableName);
		ResultSet resultSet = statement.executeQuery();
		List<SqlColumn> columns = new ArrayList<>();
		while(resultSet.next()){
			String columnName = resultSet.getString("column_name");
			boolean nullable = resultSet.getString("is_nullable").equals("YES");
			boolean autoIncrement = resultSet.getString("extra").contains("auto_increment");
			MysqlColumnType type = MysqlColumnType.parse(resultSet.getString("data_type"));
			MysqlCharacterSet characterSet = MysqlCharacterSet.parse(resultSet.getString("character_set_name"));
			Integer size = findColumnSize(resultSet);
			String defaultValue = resultSet.getString("column_default");
			SqlColumn col;
			if(characterSet == null){
				col = new SqlColumn(columnName, type, size, nullable, autoIncrement, defaultValue);
			}else{
				MysqlCollation collation = MysqlCollation.parse(resultSet.getString("collation_name"));
				col = new CharSequenceSqlColumn(columnName, type, size, nullable, autoIncrement, defaultValue,
						characterSet, collation);
			}
			columns.add(col);
		}
		return columns;
	}

	private static SqlTableIndexes fetchSqlTableIndexes(Connection connection, String schemaName, String tableName)
	throws SQLException{
		ResultSet indexList = connection.getMetaData().getIndexInfo(schemaName, schemaName, tableName, false, false);
		Set<SqlIndex> indexes = new HashSet<>();
		Set<SqlIndex> uniqueIndexes = new HashSet<>();

		SqlIndex primaryKey = null;
		String currentIndexName = null;
		List<String> currentIndexColumns = new ArrayList<>();
		boolean currentIndexUnique = false;
		while(indexList.next()){
			if(!indexList.getString("index_name").equals(currentIndexName)){
				if(currentIndexName != null){
					SqlIndex index = new SqlIndex(currentIndexName, currentIndexColumns);
					if("PRIMARY".equals(currentIndexName)){
						primaryKey = index;
					}else if(currentIndexUnique){
						uniqueIndexes.add(index);
					}else{
						indexes.add(index);
					}
				}
				currentIndexName = indexList.getString("index_name");
				currentIndexUnique = BooleanTool.isFalse(indexList.getString("non_unique"));
				currentIndexColumns = new ArrayList<>();
			}
			currentIndexColumns.add(indexList.getString("column_name"));
		}
		SqlIndex index = new SqlIndex(currentIndexName, currentIndexColumns);
		if("PRIMARY".equals(currentIndexName)){
			primaryKey = index;
		}else if(currentIndexUnique){
			uniqueIndexes.add(index);
		}else{
			indexes.add(index);
		}
		return new SqlTableIndexes(primaryKey, indexes, uniqueIndexes);
	}

	public static SqlTableMetadata fetchSqlTableMetadata(Connection connection, String schemaName, String tableName)
	throws SQLException{
		PreparedStatement statement = connection.prepareStatement("select " + ENGINE + "," + ROW_FORMAT + ","
				+ TABLE_COLLATION + " from " + INFORMATION_SCHEMA + ".tables"
				+ " where table_schema = ? and table_name = ?");
		statement.setString(1, schemaName);
		statement.setString(2, tableName);
		ResultSet resultSet = statement.executeQuery();
		resultSet.next();
		MysqlTableEngine engine = MysqlTableEngine.parse(resultSet.getString(ENGINE));
		MysqlRowFormat rowFormat = MysqlRowFormat.BY_VALUE.fromOrNull(resultSet.getString(ROW_FORMAT));
		MysqlCollation collation = MysqlCollation.parse(resultSet.getString(TABLE_COLLATION));
		MysqlCharacterSet characterSet = MysqlCharacterSet.parse(collation.name().split("_")[0]);
		return new SqlTableMetadata(engine, rowFormat, collation, characterSet);
	}

	private static Integer findColumnSize(ResultSet resultSet) throws SQLException{
		Integer size = Math.toIntExact(Math.min(Integer.MAX_VALUE,
				resultSet.getLong("character_maximum_length")));//TODO work with longs
		if(size != 0){
			return size;
		}
		size = resultSet.getInt("datetime_precision");
		if(!resultSet.wasNull()){
			return size;
		}
		Matcher matcher = COLUMN_TYPE_PATTERN.matcher(resultSet.getString("column_type"));
		if(matcher.find()){
			return Integer.parseInt(matcher.group());
		}
		return null;
	}

}