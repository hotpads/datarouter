package com.hotpads.datarouter.client.imp.jdbc.ddl;


public class SqlAlterTable {
	
	String alterTable;
	SqlAlterTypes type;
	public SqlAlterTable(String alterTable, SqlAlterTypes type){
		this.alterTable = alterTable;
		this.type = type;
	}

	public String getAlterTable() {
		return alterTable;
	}

	public void setAlterTable(String alterTable) {
		this.alterTable = alterTable;
	}

	public SqlAlterTypes getType() {
		return type;
	}

	public void setType(SqlAlterTypes type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return alterTable;
	}
	
	
}

