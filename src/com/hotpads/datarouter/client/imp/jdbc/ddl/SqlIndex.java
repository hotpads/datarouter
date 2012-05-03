package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.util.List;

import com.hotpads.util.core.ListTool;

public class SqlIndex {
	// ATTRIBUTES
	String name;
	List<SqlColumn> columns;
	
	
	//CONSTRUCTORS
	public SqlIndex(String name, List<SqlColumn> columns) {
		super();
		this.name = name;
		this.columns = columns;
	}

	public SqlIndex(String name) {
		super();
		this.name = name;
		columns=ListTool.createArrayList();
	}
	
}
