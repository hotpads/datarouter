package com.hotpads.datarouter.client.imp.mysql.ddl.generate.imp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hotpads.datarouter.client.imp.mysql.ddl.domain.CharSequenceSqlColumn;
import com.hotpads.datarouter.client.imp.mysql.ddl.domain.MySqlCharacterSet;
import com.hotpads.datarouter.client.imp.mysql.ddl.domain.MySqlCollation;
import com.hotpads.datarouter.client.imp.mysql.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.mysql.ddl.domain.MySqlRowFormat;
import com.hotpads.datarouter.client.imp.mysql.ddl.domain.MySqlTableEngine;
import com.hotpads.datarouter.client.imp.mysql.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.mysql.ddl.domain.SqlIndex;
import com.hotpads.datarouter.client.imp.mysql.ddl.domain.SqlTable;
import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.datarouter.util.core.DrBooleanTool;

public class ConnectionSqlTableGenerator{

	private static final Pattern COLUMN_TYPE_PATTERN = Pattern.compile("\\d+");

	public static SqlTable generate(JdbcConnectionPool connectionPool, String tableName, String schemaName){
		try(Connection connection = connectionPool.checkOut()){
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM information_schema.`COLUMNS` "
					+ "WHERE table_schema = ? AND table_name = ?");
			statement.setString(1, schemaName);
			statement.setString(2, tableName);
			ResultSet resultSet = statement.executeQuery();
			Map<String,SqlColumn> columnsByName = new HashMap<>();
			while(resultSet.next()){
				String columnName = resultSet.getString("column_name");
				boolean nullable = resultSet.getBoolean("is_nullable");
				boolean autoIncrement = resultSet.getString("extra").contains("auto_increment");
				MySqlColumnType type = MySqlColumnType.parse(resultSet.getString("data_type"));
				MySqlCharacterSet characterSet = MySqlCharacterSet.parse(resultSet.getString("character_set_name"));
				Integer size = findColumnSize(resultSet);
				String defaultValue = resultSet.getString("column_default");
				SqlColumn col;
				if(characterSet == null){
					col = new SqlColumn(columnName, type, size, nullable, autoIncrement, defaultValue);
				}else{
					MySqlCollation collation = MySqlCollation.parse(resultSet.getString("collation_name"));
					col = new CharSequenceSqlColumn(columnName, type, size, nullable, autoIncrement, defaultValue,
							characterSet, collation);
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

			resultSet = statement.executeQuery("select engine, row_format from information_schema.tables "
					+ "where table_name='" + tableName + "' and table_schema = '" + schemaName + "';");
			resultSet.next();
			MySqlTableEngine engine = MySqlTableEngine.parse(resultSet.getString(1));
			MySqlRowFormat rowFormat = MySqlRowFormat.fromPersistentStringStatic(resultSet.getString(2));

			String sql = "SELECT T.table_collation "
					+ "FROM information_schema.`TABLES` T, "
					+ "information_schema.`COLLATION_CHARACTER_SET_APPLICABILITY` CCSA"
					+ "\nWHERE CCSA.collation_name = T.table_collation "
					+ "\nAND T.table_schema=\"" + schemaName + "\" "
					+ "\nAND T.table_name=\"" + tableName + "\";";
			resultSet = statement.executeQuery(sql);
			resultSet.next();
			MySqlCollation collation = MySqlCollation.parse(resultSet.getString(1));

			sql = "SELECT CCSA.character_set_name "
					+ "FROM information_schema.`TABLES` T, "
					+ "information_schema.`COLLATION_CHARACTER_SET_APPLICABILITY` CCSA"
					+ "\nWHERE CCSA.collation_name = T.table_collation "
					+ "\nAND T.table_schema=\"" + schemaName + "\" "
					+ "\nAND T.table_name=\"" + tableName + "\";";
			resultSet = statement.executeQuery(sql);
			resultSet.next();
			MySqlCharacterSet characterSet = MySqlCharacterSet.parse(resultSet.getString(1));

			return new SqlTable(tableName, primaryKey, new ArrayList<>(columnsByName.values()), indexes,
					uniqueIndexes, characterSet, collation, rowFormat, engine);
		}catch(SQLException e){
			throw new RuntimeException(e);
		}
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