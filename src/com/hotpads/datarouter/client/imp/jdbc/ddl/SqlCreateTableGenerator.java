package com.hotpads.datarouter.client.imp.jdbc.ddl;

import org.junit.Test;

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
		int numbreOfIndexes=table.getIndexes().size();
		for(int i=0; i< numbreOfIndexes; i++){
			s+=" KEY `"+ table.getIndexes().get(i).getName() +"` (";
			int numberOfColumndInindexe = table.getIndexes().get(i).getColumns().size();
			for(int j=0; j< numberOfColumndInindexe; j++){
				col = table.getIndexes().get(i).getColumns().get(j);
				s+= "`" + table.getIndexes().get(i).getColumns().get(j).getName() + "`";
						if(i != numberOfColumndInindexe -1) {
							s+="," ;
						}
			}
			s+=")\n";
		}
		
		s+=") ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='InnoDB free: 397312 kB'";
		return s;
		
	}
	
	public static class  SqlCreateTableGeneratorTests{
		
		@Test public void testGenerate(){
			String nameOfTable="Model";
			SqlColumn col1 = new SqlColumn("includeInSummary", MySqlColumnType.TINYINT, 1, true);
			SqlColumn col2 = new SqlColumn("feedModelId", MySqlColumnType.VARCHAR, 100, false);
			SqlColumn col3 = new SqlColumn("feedListingId", MySqlColumnType.VARCHAR, 100, false);
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
			System.out.println(generator.generate());
			//Assert.assertEquals(expected, actual);
		}
	}
}
