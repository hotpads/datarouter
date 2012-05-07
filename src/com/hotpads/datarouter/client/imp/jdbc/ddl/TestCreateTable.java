package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.test.DRTestConstants;

public class TestCreateTable {

	@Test public void testCreateTable() throws SQLException{
		String tableName = "Person";
		SqlColumn colLastName = new SqlColumn("lastName", MySqlColumnType.VARCHAR);
		SqlColumn colFirstName = new SqlColumn("firstName", MySqlColumnType.VARCHAR);
		SqlTable inputTable = new SqlTable(tableName)
				.addColumn(colLastName)
				.addColumn(colFirstName)
				.addColumn(new SqlColumn("weight", MySqlColumnType.INT))
				.addColumn(new SqlColumn("age", MySqlColumnType.INT))
				.setPrimaryKey(new SqlIndex("primaryKey")
						.addColumn(colLastName)
						.addColumn(colFirstName));
		SqlCreateTableGenerator generator = new SqlCreateTableGenerator(inputTable);
		String sql = generator.generate();
		System.out.println(sql);
		
		Connection conn = JdbcTool.openConnection("localhost", 3306, "drTest0", "root", "");
		Statement stmt = null;
		try{
			stmt = conn.createStatement();
			stmt.execute("drop table "+inputTable.getName()+";");
			stmt.execute("use "+DRTestConstants.DATABASE_drTest0+";");
			stmt.execute(sql);
			
			String showCreateTableOutput = stmt.execute("show create table "+inputTable.getName()+";");
			//parse showCreateTableOutput into a SqlTable "outputTable"
			//assert inputTable.equals(outputTable)
		}catch(Exception e) {
			e.printStackTrace();			
		}finally{
			if(stmt!=null){ stmt.close(); }
			if(conn!=null){ conn.close(); }
		}
	}
	
}
