package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;

public class FieldSqlTableGenerator {
	private String tableName;
	private List<Field<?>> primaryKeyFields;
	private List<Field<?>> nonKeyFields;
	
	public FieldSqlTableGenerator(String tableName, List<Field<?>> primaryKeyFields,
			List<Field<?>> nonKeyFields) {
		this.tableName = tableName;
		this.primaryKeyFields = primaryKeyFields;
		this.nonKeyFields = nonKeyFields;
	}

	public SqlTable generate(){
		SqlTable table = new SqlTable(getTableName());
		SqlIndex pKey = new SqlIndex(getTableName() +" primary key");
		for(Field<?> f: getPrimaryKeyFields()){
			pKey.addColumn(f.getSqlColumnDefinition());
		}
		table.setPrimaryKey(pKey);
		for(Field<?> f: getNonKeyFields()){
			table.addColumn(f.getSqlColumnDefinition());
		}
		return table;
		
	}
	
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public List<Field<?>> getPrimaryKeyFields() {
		return primaryKeyFields;
	}

	public void setPrimaryKeyFields(List<Field<?>> primaryKeyFields) {
		this.primaryKeyFields = primaryKeyFields;
	}

	public List<Field<?>> getNonKeyFields() {
		return nonKeyFields;
	}

	public void setNonKeyFields(List<Field<?>> nonKeyFields) {
		this.nonKeyFields = nonKeyFields;
	}
	
	

}
