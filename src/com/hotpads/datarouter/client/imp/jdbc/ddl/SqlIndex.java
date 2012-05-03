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

	
	public void addColumn(SqlColumn col){
		columns.add(col);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<SqlColumn> getColumns() {
		return columns;
	}

	public void setColumns(List<SqlColumn> columns) {
		this.columns = columns;
	}

	@Override
	public String toString() {
		return "SqlIndex [name=" + name + ", columns=" + columns + "]";
	}

	
	
}
