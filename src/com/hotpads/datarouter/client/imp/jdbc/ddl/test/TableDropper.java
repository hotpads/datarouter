package com.hotpads.datarouter.client.imp.jdbc.ddl.test;

import java.sql.Connection;
import java.sql.Statement;

import org.junit.Test;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;

public class TableDropper{

	@Test public void testCreateAndDeleteTable()throws Exception{
		
		Connection conn = JdbcTool.openConnection("localhost", 3306, "drTest0", "root", "");
		Statement stmt = null;
		try{
			stmt = conn.createStatement();
//			stmt.execute("drop table Cheese");
//			stmt.execute("drop table if exists Inquiry;");	
			stmt.execute("alter table Cheese \n" + "add column blabla BOOLEAN DEFAULT NULL;");
		}catch(Exception e) {
			e.printStackTrace();			
		}finally{
			if(stmt!=null){ stmt.close(); }
			if(conn!=null){ conn.close(); }
		}
	}
}
