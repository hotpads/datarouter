package com.hotpads.datarouter.client.imp.jdbc.ddl.test;

import java.sql.SQLException;

import org.junit.Test;

public class CreateTableTester {

	@Test public void testCreateTable() throws SQLException{
//		String tableName = "Person";
//		SqlColumn colLastName = new SqlColumn("lastName", MySqlColumnType.VARCHAR,200,false);
//		SqlColumn colFirstName = new SqlColumn("firstName", MySqlColumnType.VARCHAR,200,false);
//		List<SqlIndex> index = ListTool.createArrayList();
//		index.add(new SqlIndex("index1").addColumn(colLastName).addColumn(colFirstName));
//		SqlTable inputTable = new SqlTable(tableName)
//				.addColumn(colLastName)
//				.addColumn(colFirstName)
//				.addColumn(new SqlColumn("weight", MySqlColumnType.INT,200,true))
//				.addColumn(new SqlColumn("age", MySqlColumnType.INT,130,true))
//				.setPrimaryKey(new SqlIndex("Person Primary Key")
//						.addColumn(colLastName)
//						.addColumn(colFirstName));
//		inputTable.setIndexes(index);
//		
//		SqlCreateTableGenerator generator = new SqlCreateTableGenerator(inputTable);
//		String sql = generator.generateDdl();
//		
//		Connection conn = JdbcTool.openConnection("localhost", 3306, DRTestConstants.DATABASE_drTest0, "root", "");
//		Statement stmt = null;
//		try{
//			stmt = conn.createStatement();
//			stmt.execute("drop table if exists "+inputTable.getName()+";");
//			System.out.println(sql);
//			stmt.execute(sql);
//			
//			ResultSet result = stmt.executeQuery("show create table "+inputTable.getName() + ";");
//			result.next();// position at first result row
//			String showCreateTableOutput = result.getString(2);
//			
//			ConnectionSqlTableGenerator parser = new ConnectionSqlTableGenerator(showCreateTableOutput);
//			SqlTable outputTable = parser.parse();
//			Assert.assertEquals(inputTable, outputTable);
//			
//			System.out.println("/******************** INPUT TABLE  *********************************/");
//			System.out.println(inputTable);
//			System.out.println("/*******************  OUTPUT TABLE **********************************/");
//			System.out.println(outputTable);
//			 
//		}catch(Exception e) {
//			e.printStackTrace();			
//		}finally{
//			if(stmt!=null){ stmt.close(); }
//			if(conn!=null){ conn.close(); }
//		}
	}

}
