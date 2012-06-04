package com.hotpads.datarouter.client.imp.jdbc.ddl.generate.imp;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.junit.Test;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlTableEngine;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlIndex;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.SqlTableGenerator;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;

public class ConnectionSqlTableGenerator implements SqlTableGenerator{

	protected Connection connection;
	protected String tableName;

	public ConnectionSqlTableGenerator(Connection connection, String tableName){
		super();
		this.connection = connection;
		this.tableName = tableName;
	}

	@Override
	public SqlTable generate(){
		SqlTable table = new SqlTable(tableName);
		try{
			Statement stmt = connection.createStatement();
			stmt = connection.createStatement();
			String sql = "select * from " + tableName;
			ResultSet rs = stmt.executeQuery(sql);
			ResultSetMetaData metaData = rs.getMetaData();
			int rowCount = metaData.getColumnCount();
//			System.out.println("Table Name : " + metaData.getTableName(2));
			// System.out.println("Field \tsize\tDataType");
			
			for(int i = 0; i < rowCount; i++){
				boolean nullable = true; // nullable by default
				if(metaData.isNullable(i + 1) == ResultSetMetaData.columnNoNulls) nullable = false;
				MySqlColumnType type = MySqlColumnType.parse(metaData.getColumnTypeName(i + 1));
				SqlColumn col = new SqlColumn(metaData.getColumnName(i + 1), type,
						metaData.getColumnDisplaySize(i + 1), nullable);
				table.addColumn(col);
			}
			
			DatabaseMetaData dbmd = connection.getMetaData();
			ResultSet indexList = dbmd.getIndexInfo(null, null, tableName, false, false);
			List<String> listOfIndexNames = ListTool.createArrayList();
			List<SqlIndex> listOfIndexes = ListTool.createArrayList();
			
			while(indexList.next()){
				String indexName = indexList.getString("INDEX_NAME");
				if(!listOfIndexNames.contains(indexName)){
					listOfIndexNames.add(indexName);
					SqlIndex index = new SqlIndex(indexName);
					if(indexName.toUpperCase().equals("PRIMARY")){
						addAppropriateColumnToPrimaryKeyFromListOfColumnindexList(table, indexList
								.getString("COLUMN_NAME"), table.getColumns());
						// listOfIndexes.add(index);
					}else{
						addAppropriateColumnToIndexFromListOfColumn(index, indexList.getString("COLUMN_NAME"), table
								.getColumns());
						listOfIndexes.add(index);
					}

				}else{ // already created this index, just add a column to it
					addAppropriateColumnToAppropriateIndexFromListOfColumn(indexName, listOfIndexes, indexList
							.getString("COLUMN_NAME"), table.getColumns());
				}
			}
			
			for(SqlIndex i : listOfIndexes){
				table.addIndex(i);
			}
			indexList.close();
			
			rs = stmt.executeQuery("select engine from information_schema.tables where table_name='" + tableName + "';");
			rs.next();
			table.setEngine(MySqlTableEngine.parse(rs.getString(1)));
			
		}catch(SQLException e){
			e.printStackTrace();
		}

		return table;
	}

	private void addAppropriateColumnToPrimaryKeyFromListOfColumnindexList(SqlTable table, String string,
			List<SqlColumn> columns){
		for(SqlColumn col : IterableTool.nullSafe(columns)){
			if(col.getName().equals(string)) table.getPrimaryKey().addColumn(col);
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
		for(SqlColumn col : IterableTool.nullSafe(columns)){
			if(col.getName().equals(string)) index.addColumn(col);
		}
	}

	private static void addAppropriateColumnToIndexFromListOfColumn(SqlIndex index, String s1, List<SqlColumn> columns){
		for(SqlColumn col : IterableTool.nullSafe(columns)){
			if(col.getName().equals(s1)) index.addColumn(col);
		}
	}

	public static class TestSqlCreateTableFromConnection{
		@Test
		public void getTableTest() throws SQLException{
			Connection conn = JdbcTool.openConnection("localhost", 3306, "property", "root", "");

			ConnectionSqlTableGenerator creator;

			List<String> tableNames = JdbcTool.showTables(conn);
			SqlTable table;

			for(String s : tableNames){
				System.out.println(s);
				creator = new ConnectionSqlTableGenerator(conn, s);
				if(s.equals("CommentVote")){
					System.out.println();
				}
				table = creator.generate();
				System.out.println(table);
				System.out.println();
			}

			creator = new ConnectionSqlTableGenerator(conn, "UserNote");
			table = creator.generate();
			System.out.println(table);
			conn.close();
		}
	}

}