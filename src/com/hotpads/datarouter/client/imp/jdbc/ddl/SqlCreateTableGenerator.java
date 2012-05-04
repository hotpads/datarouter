package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.util.List;

import org.junit.Test;

import com.hotpads.util.core.ListTool;

import junit.framework.Assert;

public class SqlCreateTableGenerator {

	/************************** fields *************************/
	
	protected SqlTable table;

	
	/******************* constructors ****************************/
	
	public SqlCreateTableGenerator(SqlTable table){
		this.table = table;
	}
	
	/****************** primary method ****************************/

	public String generate() {
		String s="CREATE TABLE `" + table.getName()+"` (\n"; 
		int nuimberOfColumns=table.getColumns().size();
		SqlColumn col;

		for(int i=0; i<nuimberOfColumns; i++){
			col = table.getColumns().get(i);
			s+= " `" + col.getName() + "` " +col.getType().toString().toLowerCase();
			if(col.getMaxLength()!=null){
				s+="(" + col.getMaxLength() + ") ";
			}
			if(col.getNullable()){
				s+="DEFAULT NULL,\n";
			}
			else{
				s+="NOT NULL DEFAULT,\n";
			}
		}

		s+=" PRIMARY KEY ("; 
		 int numberOfColumnsInPrimaryKey=table.getPrimaryKey().getColumns().size();
		for(int i=0; i< numberOfColumnsInPrimaryKey; i++){
			col = table.getPrimaryKey().getColumns().get(i);
			s+= "`" + col.getName() + "`";
					if(i != numberOfColumnsInPrimaryKey -1) {
						s+="," ;
					}
		}
		s+=")\n";
		s+=") ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 397312 kB'":
		return s;
		
	}
	
	public static class  SqlCreateTableGeneratorTests{
		
		@Test public void testGenerate(){
			String nameOfTable="Model";
			SqlTable myTable = new SqlTable(nameOfTable);
			SqlColumn col1=new SqlColumn("includeInSummary", MySqlColumnType.TINYINT, 1, true),
					col2 = new SqlColumn("feedModelId", MySqlColumnType.VARCHAR, 100, false),
					col3 = new SqlColumn("feedListingId", MySqlColumnType.VARCHAR, 100, false);
			myTable.addColumn(col1);
			myTable.addColumn(col2);
			myTable.addColumn(col3);
			List<SqlColumn> list = ListTool.createArrayList();
			list.add(col1);
			list.add(col2);
			list.add(col3);
			SqlIndex primaryKey = new SqlIndex("PKey", list);
			myTable.setPrimaryKey(primaryKey);
			SqlCreateTableGenerator generator = new SqlCreateTableGenerator(myTable);
			System.out.println(generator.generate());
			//Assert.assertEquals(expected, actual);
		}
	}
}
