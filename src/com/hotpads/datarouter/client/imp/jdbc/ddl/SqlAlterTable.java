package com.hotpads.datarouter.client.imp.jdbc.ddl;


public class SqlAlterTable {
	
	String alterTable;
	SqlAlterTableTypes type;
	public SqlAlterTable(String alterTable, SqlAlterTableTypes type){
		this.alterTable = alterTable;
		this.type = type;
	}

	public String getAlterTable() {
		return alterTable;
	}

	public void setAlterTable(String alterTable) {
		this.alterTable = alterTable;
	}

	public SqlAlterTableTypes getType() {
		return type;
	}

	public void setType(SqlAlterTableTypes type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return alterTable;
	}
	
	
}

