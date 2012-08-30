package com.hotpads.datarouter.client.imp.jdbc.ddl;

import org.junit.Test;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlColumn;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlIndex;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.util.core.StringTool;

public class SqlCreateTableGenerator implements DdlGenerator{

	/************************** fields *************************/
	
	protected SqlTable table;
	protected String databaseName="";
	
	/******************* constructors ****************************/
	
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
		StringBuilder sb=new StringBuilder("CREATE TABLE " );
		if(!StringTool.isEmpty(databaseName)){
			sb.append(databaseName + ".");
		}
		sb.append(table.getName()+" (\n"); 
		int nuimberOfColumns=table.getColumns().size();
		SqlColumn col;
		String typeSring;
		MySqlColumnType type;
		for(int i=0; i<nuimberOfColumns; i++){
			col = table.getColumns().get(i);
			type = col.getType();
			typeSring = type.toString().toLowerCase();
			sb.append(" " + col.getName() + " " + typeSring);
			if(col.getMaxLength()!=null && type.isSpecifyLength()){
				sb.append("(" + col.getMaxLength() + ")");
			}
			if(col.getNullable()){
				sb.append(" default null");
			}else{
				sb.append(" not null");
			}
			if(i<nuimberOfColumns-1) sb.append(",\n");
		}
		
		if(table.hasPrimaryKey()){
				sb.append(",\n");
				sb.append(" primary key ("); 
			int numberOfColumnsInPrimaryKey=table.getPrimaryKey().getColumns().size();
			for(int i=0; i< numberOfColumnsInPrimaryKey; i++){
				col = table.getPrimaryKey().getColumns().get(i);
				sb.append(col.getName());
						if(i != numberOfColumnsInPrimaryKey -1){
							sb.append(",");
						}
			}
			sb.append(")");
		}
		
		int numberOfIndexes=table.getIndexes().size();
		if(numberOfIndexes>0) sb.append(",\n");
		for(int i=0; i< numberOfIndexes; i++){
			sb.append(" KEY "+ table.getIndexes().get(i).getName() +" (");
			int numberOfColumndInIndexe = table.getIndexes().get(i).getColumns().size();
			for(int j=0; j< numberOfColumndInIndexe; j++){
				col = table.getIndexes().get(i).getColumns().get(j);
				sb.append(table.getIndexes().get(i).getColumns().get(j).getName());
						if(j != numberOfColumndInIndexe -1){
							sb.append(", ") ;
						}
			}
			sb.append(")");
			if(i != numberOfIndexes - 1){
				sb.append(",");
			}
			sb.append("\n");
		}
		sb.append(")");
		sb.append(" engine=" +
				table.getEngine() +
				" character set = " +table.getCharacterSet() + " collate " +table.getCollation());
		return sb.toString();
		
	}
	
	public static class  SqlCreateTableGeneratorTester{
		
		@Test public void testGenerate(){
			String nameOfTable="Model";
			SqlColumn col1 = new SqlColumn("includeInSummary", MySqlColumnType.TINYINT, 1, true);
			SqlColumn col2 = new SqlColumn("feedModelId", MySqlColumnType.VARCHAR, 100, false);
			SqlColumn col3 = new SqlColumn("feedListingId", MySqlColumnType.DATETIME, 19, true);//new SqlColumn("feedListingId", MySqlColumnType.VARCHAR, 100, false);
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
