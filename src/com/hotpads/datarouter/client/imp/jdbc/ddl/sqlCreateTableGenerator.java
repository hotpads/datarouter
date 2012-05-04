package com.hotpads.datarouter.client.imp.jdbc.ddl;

public class sqlCreateTableGenerator {
	
	protected SqlTable table;
	
	public sqlCreateTableGenerator(SqlTable table){
		this.table=table;
	}
	
	public String generate(){
		String s="CREATE TABLE `" + table.getName() 
				+ "` (\n";
		for (SqlColumn col: table.getColumns()) {
			s+= "`" + col.getName() +"` " + col.getType().toString();
					if(col.getMaxLength()==null){
						s+="(" +col.getMaxLength()+") ";
					}
					if(col.getNullable()){
						s+="DEFAULT NULL,\n";
					} else{
						s+="NOT NULL,";
					}
		}
		s+= "PRIMARY KEY (" ;
		int numberOfColumnsInPrimaryKey=table.getPrimaryKey().getColumns().size();
		for(int i=0; i< numberOfColumnsInPrimaryKey; i++){
			s+= "`" +table.getPrimaryKey().getColumns().get(i).getName() +"`";
			if(i<numberOfColumnsInPrimaryKey-1){
				s+=",";
			}
		}
		
		for (int i=0; i<table.getIndexes().size(); i++) {
			table.getIndexes().get(i);
		}
		return s;
	}
}
