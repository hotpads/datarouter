package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.Assert;

import org.junit.Test;

import sun.net.www.content.text.plain;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.test.DRTestConstants;

public class TestCreateTable {

	@Test public void testCreateTable() throws SQLException{
		String tableName = "Person";
		SqlColumn colLastName = new SqlColumn("lastName", MySqlColumnType.VARCHAR,200,false);
		SqlColumn colFirstName = new SqlColumn("firstName", MySqlColumnType.VARCHAR,200,false);
		SqlTable inputTable = new SqlTable(tableName)
				.addColumn(colLastName)
				.addColumn(colFirstName)
				.addColumn(new SqlColumn("weight", MySqlColumnType.INT,200,true))
				.addColumn(new SqlColumn("age", MySqlColumnType.INT,130,true))
				.setPrimaryKey(new SqlIndex("Person Primary Key")
						.addColumn(colLastName)
						.addColumn(colFirstName));
		SqlCreateTableGenerator generator = new SqlCreateTableGenerator(inputTable);
		String sql = generator.generate();
		
		Connection conn = JdbcTool.openConnection("localhost", 3306, "drTest0", "root", "");
		Statement stmt = null;
		try{
			stmt = conn.createStatement();
			stmt.execute("use "+DRTestConstants.DATABASE_drTest0+";");
			//sql = "create table "+inputTable.getName()+";" + sql;
			stmt.execute(sql);
			
			String showCreateTableOutput="";
			 ResultSet result = stmt.executeQuery("show create table "+inputTable.getName() + ";");
			 while(result.next()){
				 showCreateTableOutput+=result.getString(2);
				// System.out.println(showCreateTableOutput);
			 }
			//parse showCreateTableOutput into a SqlTable "outputTable"
			 SqlCreateTableParser parser = new SqlCreateTableParser(showCreateTableOutput);
			 SqlTable outputTable = parser.parse();
			//assert inputTable.equals(outputTable)
			 System.out.println("/******************** INPUT TABLE  *********************************/");
			 System.out.println(inputTable);
			 System.out.println("/*******************  OUTPUT TABLE **********************************/");
			 System.out.println(outputTable);
			 stmt.execute("drop table "+inputTable.getName()+";");
			 Assert.assertEquals(inputTable, outputTable);
			 
			 
		}catch(Exception e) {
			e.printStackTrace();			
		}finally{
			if(stmt!=null){ stmt.close(); }
			if(conn!=null){ conn.close(); }
		}
	}
	
}
