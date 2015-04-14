package com.hotpads.datarouter.client.imp.jdbc.ddl.generate.imp;

import org.junit.Test;


public class ConnectionSqlTableGeneratorTest{

	@Test
	public void testGenerate(){
//		Connection connection = JdbcTool.openConnection("localhost", 3306, "property", "root", "");
//		//Statement st = null;
//		List<String> tableNames = JdbcTool.showTables(connection);
//		SchemaUpdateOptions options = new SchemaUpdateOptions().setAllFalse();
//		//st = connection.createStatement();
//		//ResultSet rs = st.executeQuery("");
////			for(String tableName : tableNames){
////				ConnectionSqlTableGenerator executeConstructor = new ConnectionSqlTableGenerator(connection, tableName);
////				SqlTable executeCurrent = executeConstructor.generate();
////				//SqlAlterTableGenerator executeAlterTableGenerator = new SqlAlterTableGenerator(
////				//		schemaUpdateExecuteOptions, executeCurrent, requested, JdbcTool.getSchemaName(connectionPool));
////				System.out.println(executeCurrent);
////				System.out.println("---------------------------------------------------------------------------------");
////			}
//		ConnectionSqlTableGenerator constructor = new ConnectionSqlTableGenerator(connection, "BlockedContact");
//		SqlTable current = constructor.generate();
//		SqlTable requested = null;
//		SqlAlterTableGenerator alterTableGenerator = new SqlAlterTableGenerator(options, current, requested, "BlockedContact");
//		fail("Not yet implemented");
	}

}
