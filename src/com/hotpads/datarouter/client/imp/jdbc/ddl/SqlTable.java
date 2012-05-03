package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.util.List;

import com.hotpads.util.core.ListTool;

public class SqlTable {
	
	// ATTRIBUTES
	String name;
	List<SqlColumn> columns;
	SqlIndex primaryKey;
	List<SqlIndex> indexes;
	
	// CONSTRUCTORS
	public SqlTable(String name, List<SqlColumn> columns, SqlIndex primaryKey,
			List<SqlIndex> indexes) {
		super();
		this.name = name;
		this.columns = columns;
		this.primaryKey = primaryKey;
		this.indexes = indexes;
	}

	public SqlTable(String name, List<SqlColumn> columns) {
		super();
		this.name = name;
		this.columns = columns;
		this.indexes = ListTool.createArrayList();
	}
	
	public SqlTable(String name) {
		super();
		this.name = name;
		columns = ListTool.createArrayList();
		this.indexes = ListTool.createArrayList();
	}

	public static SqlTable parseCreateTable(String createTableStatement){
		return null;
	}
	public String getCreateTable(){
		return null;
	}

	// GETTERS & SETTERS
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

	public SqlIndex getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(String s) {
		for(SqlColumn col : columns){
			if(col.getName().equals(s)){
				//this.primaryKey =  col;
				System.out.println("The primary key is " + col);
			}
		}
			}

	public List<SqlIndex> getIndexes() {
		return indexes;
	}

	public void setIndexes(List<SqlIndex> indexes) {
		this.indexes = indexes;
	}

	@Override
	public String toString() {
		return "SqlTable [name=" + name + ", columns=" + columns + "]";
	}
	
	
}
