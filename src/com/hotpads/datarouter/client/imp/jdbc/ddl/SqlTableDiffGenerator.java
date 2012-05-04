package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.util.List;

import com.hotpads.util.core.ListTool;

public class SqlTableDiffGenerator{

	protected SqlTable current, requested;

	public SqlTableDiffGenerator(SqlTable current, SqlTable requested){
		this.current = current;
		this.requested = requested;
	}
	
	public List<SqlColumn> getColumnsToAdd(){
		List<SqlColumn> columnsToAdd = ListTool.createArrayList();
		
		return columnsToAdd;
	}
	
	public List<SqlColumn> getColumnsToRemove(){
		List<SqlColumn> columnsToAdd = ListTool.createArrayList();
		
		return columnsToAdd;
	}
	
	public List<SqlColumn> getIndexesToAdd(){
		List<SqlColumn> columnsToAdd = ListTool.createArrayList();
		
		return columnsToAdd;
	}
	
	public List<SqlColumn> getIndexesToRemove(){
		List<SqlColumn> columnsToAdd = ListTool.createArrayList();
		
		return columnsToAdd;
	}
	
	public boolean isTableModified() {
		return true;//TODO calculate
	}
	
	public boolean isPrimaryKeyModified() {
		return false; //TODO calculate
	}
	
	//etc
	
}
