package com.hotpads.datarouter.client.imp.jdbc.ddl;


public class SqlAlterTable {
	
	SqlTableDiffGenerator diffGenerator;
	
	public SqlAlterTable(SqlTableDiffGenerator diffGenerator){
		this.diffGenerator = diffGenerator;
	}
	
	public String getAlterTableStatement(){
		StringBuilder sb = new StringBuilder();
		sb.append("alter table "+diffGenerator.getRequested().getName()+"(");
		//TODO append 
		return sb.toString();
	}
}
