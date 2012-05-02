package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.util.List;

import com.hotpads.util.core.ListTool;

public class SqlTable {
	
	// ATTRIBUTES
	String name;
	List<SqlColumn> columns;
	
	
	
	public SqlTable(String name, List<SqlColumn> columns) {
		super();
		this.name = name;
		this.columns = columns;
	}
	
	public SqlTable(String name) {
		super();
		this.name = name;
		columns = ListTool.createArrayList();
	}

	public static SqlTable parseCreateTable(String createTableStatement){
		return null;
		
	}
	public String getCreateTable(){
		return null;
		
	}

	@Override
	public String toString() {
		return "SqlTable [name=" + name + ", columns=" + columns + "]";
	}
	
	
}
