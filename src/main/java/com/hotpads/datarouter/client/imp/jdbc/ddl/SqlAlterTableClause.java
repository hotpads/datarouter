package com.hotpads.datarouter.client.imp.jdbc.ddl;


public class SqlAlterTableClause{
	
	protected String alterTable;
	protected SqlAlterTypes type;
	
	public SqlAlterTableClause(String alterTable, SqlAlterTypes type){
		this.alterTable = alterTable;
		this.type = type;
	}

	public String getAlterTable(){
		return alterTable;
	}

	public void setAlterTable(String s){
		this.alterTable = s;
	}

	public SqlAlterTypes getType(){
		return type;
	}

//	public void setType(SqlAlterTypes type){
//		this.type = type;
//	}

	@Override
	public String toString(){
		return alterTable;
	}

}

