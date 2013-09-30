package com.hotpads.datarouter.client.imp.jdbc.ddl;

import junit.framework.Assert;

import org.junit.Test;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlIndex;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.StringTool;

public class SqlCreateTableGenerator implements DdlGenerator{

	/************************** fields *************************/
	
	protected SqlTable table;
	protected String databaseName="";
	
	
	/******************* construct ****************************/
	
	public SqlCreateTableGenerator(SqlTable table){
		this.table = table;
	}
	public SqlCreateTableGenerator(SqlTable table, String databaseName){
		this.table = table;
		this.databaseName = databaseName;
	}
	
	
	/****************** primary method ****************************/

	@Override
	public String generateDdl(){
		StringBuilder sb=new StringBuilder("create table " );
		if(!StringTool.isEmpty(databaseName)){
			sb.append(databaseName + ".");
		}
		sb.append(table.getName() + " (\n");
		int numberOfColumns = table.getColumns().size();
		SqlColumn col;
		String typeString;
		MySqlColumnType type;
		for(int i=0; i<numberOfColumns; i++){
			col = table.getColumns().get(i);
			type = col.getType();
			typeString = type.toString().toLowerCase();
			sb.append(" " + col.getName() + " " + typeString);
			if(col.getMaxLength()!=null && type.isSpecifyLength()){
				sb.append("(" + col.getMaxLength() + ")");
			}
			if(col.getNullable()){
				sb.append(" default null");
			}else{
				sb.append(" not null");
			}
			if (col.getAutoIncrement()) {
				sb.append(" auto_increment");
			}
			if(i < numberOfColumns-1){ sb.append(",\n"); }
		}
		
		if(table.hasPrimaryKey()){
			sb.append(",\n");
			sb.append(" primary key ("); 
			int numberOfColumnsInPrimaryKey=table.getPrimaryKey().getColumns().size();
			for(int i=0; i< numberOfColumnsInPrimaryKey; i++){
				col = table.getPrimaryKey().getColumns().get(i);
				sb.append(col.getName());
				if(i != numberOfColumnsInPrimaryKey -1){ sb.append(","); }
			}
			sb.append(")");
		}
		
		int numIndexes = CollectionTool.size(table.getIndexes());
		if(numIndexes > 0){ sb.append(",\n"); }
		int indexCounter = -1;
		for(SqlIndex index : IterableTool.nullSafe(table.getIndexes())){
			++indexCounter;
			sb.append(" index "+ index.getName() +" (");
			int numColumns = CollectionTool.size(index.getColumns());
			int columnCounter = -1;
			for(SqlColumn column : IterableTool.nullSafe(index.getColumns())){
				++columnCounter;
				sb.append(column.getName());
				if(columnCounter != numColumns -1){ sb.append(", "); }
			}
			sb.append(")");
			if(indexCounter != numIndexes - 1){ sb.append(","); }
			sb.append("\n");
		}
		sb.append(")");
		sb.append(" engine=" + table.getEngine() + " character set = " + table.getCharacterSet() + " collate "
				+ table.getCollation());
		return sb.toString();
		
	}
	
	
	/******************** tests *************************/
	
	public static class  SqlCreateTableGeneratorTester{
		@Test
		public void testAutoIncrement() {
			String nameOfTable = "AutoIncrement";
			SqlColumn colId = new SqlColumn("id", MySqlColumnType.BIGINT, 8, false, true);
			SqlColumn colString = new SqlColumn("string", MySqlColumnType.VARCHAR, 100, true, false);
			SqlIndex primaryKey = new SqlIndex("PKey").addColumn(colId);
			SqlTable sqlTable = new SqlTable(nameOfTable)
					.addColumn(colId)
					.addColumn(colString)
					.setPrimaryKey(primaryKey);
			SqlCreateTableGenerator generator = new SqlCreateTableGenerator(sqlTable);
			String expected = "create table AutoIncrement (\n" + 
					 " id bigint(8) not null auto_increment,\n" + 
					 " string varchar(100) default null,\n" +
					 " primary key (id)) engine=INNODB character set = latin1 collate latin1_swedish_ci";
			System.out.println(generator.generateDdl());
			Assert.assertEquals(expected, generator.generateDdl());
		}
		
		@Test
		public void testGenerate(){
			String nameOfTable="Model";
			SqlColumn col1 = new SqlColumn("includeInSummary", MySqlColumnType.TINYINT, 1, true, false);
			SqlColumn col2 = new SqlColumn("feedModelId", MySqlColumnType.VARCHAR, 100, false, false);
			SqlColumn col3 = new SqlColumn("feedListingId", MySqlColumnType.DATETIME, 19, true,false);//new SqlColumn("feedListingId", MySqlColumnType.VARCHAR, 100, false);
			SqlIndex primaryKey = new SqlIndex("PKey")
					.addColumn(col1)
					.addColumn(col2)
					.addColumn(col3);
			SqlTable myTable = new SqlTable(nameOfTable)
					.addColumn(col1)
					.addColumn(col2)
					.addColumn(col3)
					.setPrimaryKey(primaryKey)
					.addIndex(new SqlIndex("idx1")
					.addColumn(col2));
			SqlCreateTableGenerator generator = new SqlCreateTableGenerator(myTable);
			System.out.println(generator.generateDdl());
			//Assert.assertEquals(expected, actual);
		}
	}
}
