package com.hotpads.datarouter.client.imp.jdbc.ddl.generate.imp;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.junit.Test;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.jdbc.ddl.SqlAlterTableGenerator;


public class ConnectionSqlTableGeneratorTest{

	@Test
	public void testGenerate(){
//		Connection connection = JdbcTool.openConnection("localhost", 3306, "property", "root", "");
//		//Statement st = null;
//		List<String> tableNames = JdbcTool.showTables(connection);
//		SchemaUpdateOptions options = new SchemaUpdateOptions().setAllFalse();
//		try{
//			//st = connection.createStatement();
//			//ResultSet rs = st.executeQuery("");
////			for(String tableName : tableNames){
////				ConnectionSqlTableGenerator executeConstructor = new ConnectionSqlTableGenerator(connection, tableName);
////				SqlTable executeCurrent = executeConstructor.generate();
////				//SqlAlterTableGenerator executeAlterTableGenerator = new SqlAlterTableGenerator(
////				//		schemaUpdateExecuteOptions, executeCurrent, requested, JdbcTool.getSchemaName(connectionPool));
////				System.out.println(executeCurrent);
////				System.out.println("---------------------------------------------------------------------------------");
////			}
//			ConnectionSqlTableGenerator constructor = new ConnectionSqlTableGenerator(connection, "BlockedContact");
//			SqlTable current = constructor.generate();
//			SqlTable requested = null;
//			SqlAlterTableGenerator alterTableGenerator = new SqlAlterTableGenerator(options, current, requested, "BlockedContact");
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//		fail("Not yet implemented");
	}

}
