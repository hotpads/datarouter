package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.util.List;

import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public class SqlIndex {
	
	/********************** fields *************************/
	
	protected String name;
	protected List<SqlColumn> columns;
	
	
	/********************** constructors **********************/
	
	public SqlIndex(String name, List<SqlColumn> columns) {
		super();
		this.name = name;
		this.columns = columns;
	}

	public SqlIndex(String name) {
		super();
		this.name = name;
		this.columns=ListTool.createArrayList();
	}

	
	/******************* methods ****************************/
	
	public SqlIndex addColumn(SqlColumn col){
		columns.add(col);
		return this;
	}

	
	/******************* Object methods **********************/
	
	@Override
	public String toString() {
		return "SqlIndex [name=" + name + ", columns=" + columns + "]";
	}
	
	
	/****************** get/set ****************************/
	
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
	
	public int getNumberOfColumns(){
		return CollectionTool.size(columns);
	}
}
