package com.hotpads.datarouter.client.imp.jdbc.ddl.test;

import java.sql.Connection;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SchemaUpdateOptions;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.imp.ConnectionSqlTableGenerator;

public class TestSchemaUpdateOptions{
	static Logger logger = Logger.getLogger(TestSchemaUpdateOptions.class);
	

	protected SqlTable currentTable = new SqlTable("TestSchemaUpdateOptions")
		.addColumn("a")
		.addIndex
		.setPk
	
	protected SqlTable requestedTable = new SqlTable("TestSchemaUpdateOptions")
		.addColumn("a")
		.addIndex
		.setPk
	
	
	protected SchemaUpdateOptions options;
	
	public TestSchemaUpdateOptions(){
	}
	
	
	@Test public synchronized void testDoNothing(){
		String tableName = "TestSchemaUpdateOptionsDoNothing";
		SqlTable currentTable = new SqlTable(tableName)
			.addColumn("a")
			.addIndex
			.setPk
		recreate(connection, currentTable);
		SchemaUpdateOptions doNothing = new SchemaUpdateOptions().setAllFalse();
		//execute the alter table statement
		SqlTable tableAfterUpdate = new ConnectionSqlTableGenerator(...);
		boolean columnWasAdded = tableAfterUpdate.containsColumn("x");
		Assert.assertFalse(columnWasAdded);
		
	}
	
	@Test public synchronized void testAddColumnsAndIndexes(){
		SchemaUpdateOptions addColumnsAndIndexes = new SchemaUpdateOptions().setAllFalse()
				.setAddColumns(true)
				.setAddIndexes(true);
		//execute the alter table statement
		SqlTable tableAfterUpdate = new ConnectionSqlTableGenerator(...);
		boolean columnWasAdded = tableAfterUpdate.containsColumn("x");
		Assert.assertTrue(columnWasAdded);
	}
	
	protected void recreate(Connection connection, SqlTable sqlTable){
		//drop
		//create
	}
}
