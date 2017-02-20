package com.hotpads.datarouter.client.imp.jdbc.ddl.generate.imp;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCharacterSet;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCollation;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlRowFormat;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlTableEngine;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlIndex;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.datarouter.client.imp.jdbc.field.StringJdbcFieldCodec;
import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.datarouter.util.core.DrBooleanTool;

public class ConnectionSqlTableGenerator{


	public static SqlTable generate(JdbcConnectionPool connectionPool, String tableName, String schemaName){
		try(Connection connection = connectionPool.checkOut()){
			Statement stmt = connection.createStatement();
			String sql = "select * from " + tableName + " limit 1";
			ResultSet resultSet = stmt.executeQuery(sql);
			ResultSetMetaData metaData = resultSet.getMetaData();
			int rowCount = metaData.getColumnCount();

			ResultSet informationSchemaResultSet = connection.createStatement().executeQuery("SELECT column_name, "
					+ "character_set_name, collation_name FROM information_schema.`COLUMNS` WHERE table_schema = '"
					+ schemaName + "' AND table_name = '" + tableName + "'");
			Map<String,MySqlCollation> collationByColumnName = new HashMap<>();
			Map<String,MySqlCharacterSet> characterSetByColumnName = new HashMap<>();
			while(informationSchemaResultSet.next()){
				String columnName = informationSchemaResultSet.getString("column_name");
				collationByColumnName.put(columnName, MySqlCollation.parse(informationSchemaResultSet.getString(
						"collation_name")));
				characterSetByColumnName.put(columnName, MySqlCharacterSet.parse(informationSchemaResultSet.getString(
						"character_set_name")));
			}

			Map<String,SqlColumn> columnsByName = new HashMap<>();
			for(int i = 0; i < rowCount; i++){
				boolean nullable = true; // nullable by default
				if(metaData.isNullable(i + 1) == ResultSetMetaData.columnNoNulls){
					nullable = false;
				}
				boolean autoIncrement = metaData.isAutoIncrement(i + 1);
				MySqlColumnType type = MySqlColumnType.parse(metaData.getColumnTypeName(i + 1));
				SqlColumn col;
				String columnName = metaData.getColumnName(i + 1);
				MySqlCharacterSet characterSet = characterSetByColumnName.get(columnName);
				MySqlCollation collation = collationByColumnName.get(columnName);
				if(type.equals(MySqlColumnType.VARCHAR)){
					col = StringJdbcFieldCodec.getMySqlTypeFromSize(columnName, metaData.getColumnDisplaySize(i + 1),
							nullable, characterSet, collation);
				}else{
					col = new SqlColumn(columnName, type, metaData.getColumnDisplaySize(i + 1), nullable,
							autoIncrement, characterSet, collation);
				}
				columnsByName.put(columnName, col);
			}

			ResultSet indexList = connection.getMetaData().getIndexInfo(null, null, tableName, false, false);
			Set<SqlIndex> indexes = new HashSet<>();
			Set<SqlIndex> uniqueIndexes = new HashSet<>();

			SqlIndex primaryKey = null;
			String currentIndexName = null;
			List<SqlColumn> currentIndexColumns = new ArrayList<>();
			boolean currentIndexUnique = false;
			while(indexList.next()){
				if(!indexList.getString("INDEX_NAME").equals(currentIndexName)){
					if(currentIndexName != null){
						SqlIndex index = new SqlIndex(currentIndexName, currentIndexColumns);
						if(primaryKey == null){
							primaryKey = index;
						}else if(currentIndexUnique){
							uniqueIndexes.add(index);
						}else{
							indexes.add(index);
						}
					}
					currentIndexName = indexList.getString("INDEX_NAME");
					currentIndexUnique = DrBooleanTool.isFalse(indexList.getString("NON_UNIQUE"));
					currentIndexColumns = new ArrayList<>();
				}
				currentIndexColumns.add(columnsByName.get(indexList.getString("COLUMN_NAME")));
			}
			SqlIndex index = new SqlIndex(currentIndexName, currentIndexColumns);
			if(primaryKey == null){
				primaryKey = index;
			}else if(currentIndexUnique){
				uniqueIndexes.add(index);
			}else{
				indexes.add(index);
			}

			resultSet = stmt.executeQuery("select engine, row_format from information_schema.tables "
					+ "where table_name='" + tableName + "' and table_schema = '" + schemaName + "';");
			resultSet.next();
			MySqlTableEngine engine = MySqlTableEngine.parse(resultSet.getString(1));
			MySqlRowFormat rowFormat = MySqlRowFormat.fromPersistentStringStatic(resultSet.getString(2));

			sql = "SELECT T.table_collation "
					+ "FROM information_schema.`TABLES` T, "
					+ "information_schema.`COLLATION_CHARACTER_SET_APPLICABILITY` CCSA"
					+ "\nWHERE CCSA.collation_name = T.table_collation "
					+ "\nAND T.table_schema=\"" + schemaName + "\" "
					+ "\nAND T.table_name=\"" + tableName + "\";";
			resultSet = stmt.executeQuery(sql);
			resultSet.next();
			MySqlCollation collation = MySqlCollation.parse(resultSet.getString(1));

			MySqlCharacterSet characterSet;
			sql = "SELECT CCSA.character_set_name "
					+ "FROM information_schema.`TABLES` T, "
					+ "information_schema.`COLLATION_CHARACTER_SET_APPLICABILITY` CCSA"
					+ "\nWHERE CCSA.collation_name = T.table_collation "
					+ "\nAND T.table_schema=\"" + schemaName + "\" "
					+ "\nAND T.table_name=\"" + tableName + "\";";
			resultSet = stmt.executeQuery(sql);
			resultSet.next();
			characterSet = MySqlCharacterSet.parse(resultSet.getString(1));

			return new SqlTable(tableName, primaryKey, new ArrayList<>(columnsByName.values()), indexes,
					uniqueIndexes, characterSet, collation, rowFormat, engine);
		}catch(SQLException e){
			throw new RuntimeException(e);
		}
	}

}