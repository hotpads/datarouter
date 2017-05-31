package com.hotpads.datarouter.client.imp.mysql.ddl.generate;


public class SqlAlterTableClause{

	private final CharSequence alterTable;

	public SqlAlterTableClause(CharSequence alterTable){
		this.alterTable = alterTable;
	}

	public CharSequence getAlterTable(){
		return alterTable;
	}

	@Override
	public String toString(){
		return alterTable.toString();
	}

}

