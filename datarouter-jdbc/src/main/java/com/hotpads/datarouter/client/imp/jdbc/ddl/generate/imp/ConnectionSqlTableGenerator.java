package com.hotpads.datarouter.client.imp.jdbc.ddl.generate.imp;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCharacterSet;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCollation;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlTableEngine;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlIndex;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.SqlTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.field.StringJdbcFieldCodec;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldBean;
import com.hotpads.datarouter.util.core.DrIterableTool;

public class ConnectionSqlTableGenerator implements SqlTableGenerator{

	protected Connection connection;
	protected String tableName;
	protected String schemaName;

	public ConnectionSqlTableGenerator(Connection connection, String tableName, String schemaName){
		super();
		this.connection = connection;
		this.tableName = tableName;
		this.schemaName = schemaName;
	}

	@Override
	public SqlTable generate(){
		SqlTable table = new SqlTable(tableName);
		try{
			Statement stmt = connection.createStatement();
			stmt = connection.createStatement();
			String sql = "select * from " + tableName +" limit 1";
			ResultSet rs = stmt.executeQuery(sql);
			ResultSetMetaData metaData = rs.getMetaData();
			int rowCount = metaData.getColumnCount();
			//	System.out.println("Table Name : " + metaData.getTableName(2));
			// System.out.println("Field \tsize\tDataType");
			
			for(int i = 0; i < rowCount; i++){
				boolean nullable = true; // nullable by default
				if(metaData.isNullable(i + 1) == ResultSetMetaData.columnNoNulls) nullable = false;
				boolean autoIncrement = metaData.isAutoIncrement(i + 1);
				MySqlColumnType type = MySqlColumnType.parse(metaData.getColumnTypeName(i + 1));
				SqlColumn col;
				if(type.equals(MySqlColumnType.VARCHAR)){
					col = StringJdbcFieldCodec.getMySqlTypeFromSize(metaData.getColumnName(i + 1), 
									metaData.getColumnDisplaySize(i + 1), nullable);
				}else{
					col = new SqlColumn(metaData.getColumnName(i + 1), type,
							metaData.getColumnDisplaySize(i + 1), nullable, autoIncrement);
				}
				table.addColumn(col);
			}
			
			DatabaseMetaData dbmd = connection.getMetaData();
			ResultSet indexList = dbmd.getIndexInfo(null, null, tableName, false, false);
			List<String> listOfIndexNames = new ArrayList<>();
			List<SqlIndex> listOfIndexes = new ArrayList<>();
			
			while(indexList.next()){
				String indexName = indexList.getString("INDEX_NAME");
				if(!listOfIndexNames.contains(indexName)){
					listOfIndexNames.add(indexName);
					SqlIndex index = new SqlIndex(indexName);
					if(indexName.toUpperCase().equals("PRIMARY")){
						addAppropriateColumnToPrimaryKeyFromListOfColumn(table, indexList
								.getString("COLUMN_NAME"), table.getColumns());
						// listOfIndexes.add(index);
					}else{
						addAppropriateColumnToIndexFromListOfColumn(index, indexList.getString("COLUMN_NAME"), table
								.getColumns());
						listOfIndexes.add(index);
					}

				}else{ // already created this index, just add a column to it
					if(indexName.toUpperCase().equals("PRIMARY")){ // if its the primary key it won't be in the listOfIndexes
						addAppropriateColumnToPrimaryKeyFromListOfColumn(table, indexList
								.getString("COLUMN_NAME"), table.getColumns());
					}
					addAppropriateColumnToAppropriateIndexFromListOfColumn(indexName, listOfIndexes, indexList
							.getString("COLUMN_NAME"), table.getColumns());
				}
			}
			
			for(SqlIndex i : listOfIndexes){
				table.addIndex(i);
			}
			indexList.close();
			
			rs = stmt.executeQuery("select engine from information_schema.tables where table_name='" + tableName 
					+ "';");
			rs.next();
			table.setEngine(MySqlTableEngine.parse(rs.getString(1)));
			sql = "SELECT T.table_collation " +
					"FROM information_schema.`TABLES` T, information_schema.`COLLATION_CHARACTER_SET_APPLICABILITY` CCSA" +
					"\nWHERE CCSA.collation_name = T.table_collation " +
					"\nAND T.table_schema=\"" + schemaName + "\" " +
					"\nAND T.table_name=\"" + tableName +"\";";
			rs = stmt.executeQuery(sql);
			rs.next();
			table.setCollation(MySqlCollation.parse(rs.getString(1)));
			
			sql = "SELECT CCSA.character_set_name " +
					"FROM information_schema.`TABLES` T, information_schema.`COLLATION_CHARACTER_SET_APPLICABILITY` CCSA" +
					"\nWHERE CCSA.collation_name = T.table_collation " +
					"\nAND T.table_schema=\"" + schemaName + "\" " +
					"\nAND T.table_name=\"" + tableName +"\";";
			rs = stmt.executeQuery(sql);
			rs.next();
			table.setCharSet(MySqlCharacterSet.parse(rs.getString(1)));
		}catch(SQLException e){
			throw new RuntimeException(e);
		}

		return table;
	}

	private void addAppropriateColumnToPrimaryKeyFromListOfColumn(SqlTable table, String string,
			List<SqlColumn> columns){
		for(SqlColumn col : DrIterableTool.nullSafe(columns)){
			if(col.getName().equals(string)){
				table.getPrimaryKey().addColumn(col);
			}
		}
	}

	private void addAppropriateColumnToAppropriateIndexFromListOfColumn(String indexName, List<SqlIndex> listOfIndexes,
			String string, List<SqlColumn> columns){
		SqlIndex index = null;
		for(SqlIndex i : listOfIndexes){
			if(i.getName().equals(indexName)){
				index = i;
				break;
			}
		} 
		if(index==null){
			return;
		}
//		System.out.println("index name :" +indexName);
//		System.out.println("list of columns " + columns);
//		System.out.println("string " +string );
//		System.out.println(" index " + index);
		for(SqlColumn col : DrIterableTool.nullSafe(columns)){
			if(col.getName().equals(string)){ index.addColumn(col); }
		}
	}

	private static void addAppropriateColumnToIndexFromListOfColumn(SqlIndex index, String s1, List<SqlColumn> columns){
		for(SqlColumn col : DrIterableTool.nullSafe(columns)){
			if(col.getName().equals(s1)){ index.addColumn(col); }
		}
	}

	public static class ConnectionSqlTableGeneratorTester{//localhost only
		
		@Test
		public void getTableAutoIncrementTest() throws SQLException{
			String databaseName = "drTestJdbc0";
			Connection conn = JdbcTool.openConnection("localhost", 3306, databaseName, "root", "");


			List<String> tableNames = JdbcTool.showTables(conn);
			String tableName = tableNames.get(tableNames.indexOf(ManyFieldBean.class.getSimpleName()));
			ConnectionSqlTableGenerator creator = new ConnectionSqlTableGenerator(conn, tableName, databaseName);
			SqlTable table = creator.generate();
			System.out.println(table);
			System.out.println();
					
			conn.close();
		}
		
		@Test
		public void getTableTest() throws SQLException{
			String databaseName = "property";
			Connection conn = JdbcTool.openConnection("localhost", 3306, databaseName, "root", "");

			ConnectionSqlTableGenerator creator;

			List<String> tableNames = JdbcTool.showTables(conn);
			SqlTable table;

			for(String s : tableNames){
				System.out.println(s);
				creator = new ConnectionSqlTableGenerator(conn, s, databaseName);
				if(s.equals("CommentVote")){
					System.out.println();
				}
				table = creator.generate();
				System.out.println(table);
				System.out.println();
			}

			creator = new ConnectionSqlTableGenerator(conn, "UserNote", databaseName);
			table = creator.generate();
			System.out.println(table);
			conn.close();
		}
	}

}