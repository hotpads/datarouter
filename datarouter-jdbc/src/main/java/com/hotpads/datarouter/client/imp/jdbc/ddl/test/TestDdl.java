package com.hotpads.datarouter.client.imp.jdbc.ddl.test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;

public class TestDdl{

	@Test public void testCreateAndDeleteTable() throws Exception{
		Connection conn = JdbcTool.openConnection("localhost", 3306, "drTest0", "root", "");
		Statement stmt = null;
		try{
			stmt = conn.createStatement();

			// stmt.execute("drop table Cheese");
			stmt.execute("drop table if exists Cheese;");		
			String sql = "create table Cheese " 
					+"(id varchar(30), "
					+" country char(2), "
					+" rating integer, "
					+" primary key (id))"; 

			stmt.executeUpdate(sql);

			stmt.execute("insert into Cheese values('Velveeta', 'US', 7);");
			stmt.execute("insert into Cheese values('Camembert', 'FR', 8);");

			ResultSet resultSet = stmt.executeQuery("select count(id) as cnt from Cheese");
			resultSet.next();
			int numCheeses = resultSet.getInt("cnt");
			System.out.println("num : " + numCheeses);
			Assert.assertEquals(2, numCheeses);
			
			ResultSet resultSet2 = stmt.executeQuery("show create table Cheese;");
			while(resultSet2.next()){
				// Get the data from the row using the column index
				String s = resultSet2.getString(2);
				System.out.println(s);
			}
			// ResultSet rs = stmt.executeQuery("DESCRIBE Cheese");
			ResultSet rs = conn.getMetaData().getCatalogs();

			while(rs.next()){
				System.out.println("TABLE_CAT = " + rs.getString("TABLE_CAT"));
			}

			stmt.execute("use property;");
			ResultSet resultSet3 = stmt.executeQuery("show create table User");
			while(resultSet3.next()){
				// Get the data from the row using the column index
				String s = resultSet3.getString(2);
				System.out.println(s);
			}
			
			
			conn = JdbcTool.openConnection("localhost", 3306, "property", "root", "");
			sql = "select * from Inquiry";
			rs = stmt.executeQuery(sql);
			ResultSetMetaData metaData = rs.getMetaData();
			
			int rowCount = metaData.getColumnCount();

			System.out.println("Table Name : " + metaData.getTableName(2));
			System.out.println("Field \tsize\tDataType");

			for(int i = 0; i < rowCount; i++){
				System.out.print(metaData.getColumnName(i + 1) + " \t");
				System.out.print(metaData.getColumnDisplaySize(i + 1) + "\t");
				System.out.println(metaData.getColumnTypeName(i + 1));
			}
			
			
			DatabaseMetaData dbmd = conn.getMetaData();
			ResultSet indexList = dbmd.getIndexInfo(null, null, "Inquiry", false, false);
			while(indexList.next()){
				System.out.println(" Index Name: " + indexList.getString("INDEX_NAME"));
				System.out.println(" Column Name:" + indexList.getString("COLUMN_NAME"));
			}
	        indexList.close(); 
		}finally{
			if(stmt!=null){ stmt.close(); }
			if(conn!=null){ conn.close(); }
		}
	}
}
