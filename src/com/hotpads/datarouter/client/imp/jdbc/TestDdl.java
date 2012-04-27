package com.hotpads.datarouter.client.imp.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import junit.framework.Assert;

import org.junit.Test;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;

public class TestDdl{

	@Test public void testCreateAndDeleteTable()throws Exception{
		Connection conn = JdbcTool.openConnection("localhost", 3306, "drTest0", "root", "");
		Statement stmt = null;
		try{
			stmt = conn.createStatement();
			
			stmt.execute("drop table Cheese");
						
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
			Assert.assertEquals(2, numCheeses);
		}catch(Exception e) {
			e.printStackTrace();			
		}finally{
			if(stmt!=null){ stmt.close(); }
			if(conn!=null){ conn.close(); }
		}
	}
}
