package com.hotpads.datarouter.client.imp.jdbc.ddl;

public class SqlAlterTableGenerator{

	protected SqlTable current, requested;

	public SqlAlterTableGenerator(SqlTable current, SqlTable requested){
		this.current = current;
		this.requested = requested;
	}
	
	public String generate() {
		String s="ALTER TABLE `" + current.getName()+"` (\n"; 
		//TODO everything
		
		return s;
	}
	
}
