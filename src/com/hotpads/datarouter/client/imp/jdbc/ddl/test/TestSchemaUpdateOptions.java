package com.hotpads.datarouter.client.imp.jdbc.ddl.test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.jdbc.ddl.DdlGenerator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.SqlAlterTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.SqlCreateTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.SqlTableDiffGenerator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SchemaUpdateOptions;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlIndex;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.imp.ConnectionSqlTableGenerator;
import com.hotpads.datarouter.client.type.JdbcConnectionClient;
import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public class TestSchemaUpdateOptions{
	static Logger logger = Logger.getLogger(TestSchemaUpdateOptions.class);

/*	protected SqlTable currentTable = new SqlTable("TestSchemaUpdateOptions")
		.addColumn("a")
		.addIndex
		.setPk
	
	protected SqlTable requestedTable = new SqlTable("TestSchemaUpdateOptions")
		.addColumn("a")
		.addIndex
		.setPk
	
	
	protected SchemaUpdateOptions options;*/
	
	public TestSchemaUpdateOptions(){
	}
	
	
	@Test public synchronized void testDoNothing() throws Exception{
		SchemaUpdateOptions doNothing = new SchemaUpdateOptions().setAllFalse();
		
		Connection connection = JdbcTool.openConnection("localhost", 3306, "drTest0", "root", "");
		String tableName = "TestSchemaUpdateOptionsDoNothing";
		
		SqlColumn 
		colA = new SqlColumn("A", MySqlColumnType.BIGINT,250,true),
		colB = new SqlColumn("B", MySqlColumnType.BINARY),
		colC = new SqlColumn("C", MySqlColumnType.BOOLEAN),
		colM = new SqlColumn("M", MySqlColumnType.VARCHAR);
		List<SqlColumn> 
				listBC = ListTool.createArrayList(),
				listM = ListTool.createArrayList();
		
		listBC.add(colB);
		listBC.add(colC);
		listM.add(colM);
		SqlIndex index = new SqlIndex("index1", listBC),
				index2 = new SqlIndex("index2", listM);	
		SqlTable 
				currentTable = new SqlTable(tableName).addColumn(colA).addColumn(colB).addColumn(colC),
				requestedTable = new SqlTable(tableName).addColumn(colA).addColumn(colM);
		currentTable.addIndex(index);
		requestedTable.addIndex(index2);
		
		Statement stmt = null;
				try{
					stmt = connection.createStatement();
					stmt.execute("drop table if exists "+currentTable.getName()+";");
		String sql = new SqlCreateTableGenerator(currentTable).generateDdl();
		System.out.println(sql);
		stmt.execute(sql);

		
			
		/************************ EXCECUTION TESTS ************************/
		
		DdlGenerator alterTableGenerator = new SqlAlterTableGenerator(doNothing,currentTable, requestedTable);
		System.out.println(alterTableGenerator.generateDdl());
		// TEST THAT IT DOESN'T CREATE TABLES
			
		// TEST THAT IT DOESM'T DROP TABLES 
			
		// TEST THAT IT DOESN'T ADD COLUMNS
			Assert.assertFalse(alterTableGenerator.generateDdl().toUpperCase().contains("ADD COLUMN"));
			
		// TEST THAT IT DOESN'T DELETE COLUMNS
			Assert.assertFalse(alterTableGenerator.generateDdl().toUpperCase().contains("DROP COLUMN"));
			
		// TEST THAT IT DOESN'T ADD INDEXES 
			Assert.assertFalse(alterTableGenerator.generateDdl().toUpperCase().contains("ADD INDEX"));
		
		// TEST THAT IT DOESN'T DROP INDEXES
			Assert.assertFalse(alterTableGenerator.generateDdl().toUpperCase().contains("DROP INDEX"));
	
		
		stmt.execute(alterTableGenerator.generateDdl());

		ConnectionSqlTableGenerator constructor = new ConnectionSqlTableGenerator(connection, tableName);
		currentTable = constructor.generate();
		
		// TEST THAT IT DOESN'T CREATE TABLES
		
		// TEST THAT IT DOESM'T DROP TABLES 
					
		// TEST THAT IT DOESN'T ADD COLUMNS
		Assert.assertFalse(currentTable.containsColumn("M"));
		
		// TEST THAT IT DOESN'T DELETE COLUMNS
		Assert.assertTrue(currentTable.containsColumn("A"));
		Assert.assertTrue(currentTable.containsColumn("B"));
		Assert.assertTrue(currentTable.containsColumn("C"));
					
		// TEST THAT IT DOESN'T ADD INDEXES 
		Assert.assertTrue(currentTable.containsIndex("index1"));
		 	//Assert.assertTrue(currentTable.getIndexes().equals( *** ));	
		// TEST THAT IT DOESN'T DROP INDEXES
		Assert.assertFalse(currentTable.containsIndex("index2"));
	 	//Assert.assertFalse(currentTable.getIndexes().equals( *** ));	

		}catch(Exception e) {
			e.printStackTrace();		
		}finally{
			if(stmt!=null){ stmt.close(); }
			if(connection!=null){ connection.close(); }
		}
	}
	
	@Test public synchronized void testAddColumnsAndIndexes() throws Exception{
		SchemaUpdateOptions addColumnsAndIndexes = new SchemaUpdateOptions().setAllFalse().setAddColumns(true).setAddIndexes(true);
		Connection connection = JdbcTool.openConnection("localhost", 3306, "drTest0", "root", "");
		
		String tableName = "TestSchemaUpdateOptionsDoNothing";
		
		SqlColumn 
			colA = new SqlColumn("A", MySqlColumnType.BIGINT,250,true),
			colB = new SqlColumn("B", MySqlColumnType.BINARY),
			colC = new SqlColumn("C", MySqlColumnType.BOOLEAN),
			colM = new SqlColumn("M", MySqlColumnType.VARCHAR);
		List<SqlColumn> 
				listBC = ListTool.createArrayList(),
				listM = ListTool.createArrayList();
		
		listBC.add(colB);
		listBC.add(colC);
		listM.add(colM);
		SqlIndex index = new SqlIndex("index", listBC),
				index2 = new SqlIndex("index", listM);	
		SqlTable 
				currentTable = new SqlTable(tableName).addColumn(colA).addColumn(colM),
				requestedTable = new SqlTable(tableName).addColumn(colA).addColumn(colB).addColumn(colC); 
		
		currentTable.addIndex(index);
		requestedTable.addIndex(index2);
		
		Statement stmt = null;
		try{
			stmt = connection.createStatement();
			stmt.execute("drop table if exists "+currentTable.getName()+";");
			String sql = new SqlCreateTableGenerator(currentTable).generateDdl();
			System.out.println(sql);
			stmt.execute(sql);
			DdlGenerator alterTableGenerator = new SqlAlterTableGenerator(addColumnsAndIndexes,currentTable, requestedTable);
		
		/************************ EXCECUTION TESTS ************************/
		// TEST THAT IT DOESN'T CREATE TABLES
			
		// TEST THAT IT DOESM'T DROP TABLES 
			
		// TEST THAT IT DOESN'T ADD COLUMNS
		System.out.println(alterTableGenerator.generateDdl());
			Assert.assertTrue(alterTableGenerator.generateDdl().toUpperCase().contains("ADD ("));
			
		// TEST THAT IT DOESN'T DELETE COLUMNS
			Assert.assertFalse(alterTableGenerator.generateDdl().toUpperCase().contains("DROP COLUMN"));
			
		// TEST THAT IT DOESN'T ADD INDEXES 
			Assert.assertTrue(alterTableGenerator.generateDdl().toUpperCase().contains("ADD KEY"));
		
		// TEST THAT IT DOESN'T DROP INDEXES
			Assert.assertFalse(alterTableGenerator.generateDdl().toUpperCase().contains("DROP INDEX"));
			
			stmt.execute(alterTableGenerator.generateDdl());

			ConnectionSqlTableGenerator constructor = new ConnectionSqlTableGenerator(connection, tableName);
			currentTable = constructor.generate();
			
			// TEST THAT IT DOESN'T CREATE TABLES
			
			// TEST THAT IT DOESM'T DROP TABLES 
						
			// TEST THAT IT DOES ADD COLUMNS
			Assert.assertTrue(currentTable.containsColumn("M"));
			
			// TEST THAT IT DOESN'T DELETE COLUMNS
			Assert.assertTrue(currentTable.containsColumn("A"));
			Assert.assertTrue(currentTable.containsColumn("B"));
			Assert.assertTrue(currentTable.containsColumn("C"));
						
			// TEST THAT IT DOES ADD INDEXES 
			Assert.assertTrue(currentTable.containsIndex("index1"));
			 	//Assert.assertTrue(currentTable.getIndexes().equals( *** ));	
			// TEST THAT IT DOESN'T DROP INDEXES
			Assert.assertFalse(currentTable.containsIndex("index2"));
		 	//Assert.assertFalse(currentTable.getIndexes().equals( *** ));	
		}catch(Exception e) {
			e.printStackTrace();		
		}finally{
			if(stmt!=null){ stmt.close(); }
			if(connection!=null){ connection.close(); }
		}
	}
	
	protected void recreate(Connection connection, SqlTable sqlTable){
		//drop
		//create
	}
}
