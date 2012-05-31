package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.util.core.ListTool;

public class FieldSqlTableGenerator {
	private String tableName;
	private List<Field<?>> primaryKeyFields;
	private List<Field<?>> nonKeyFields;
	private List<List<Field<?>>> indexes;
	
	public FieldSqlTableGenerator(String tableName, List<Field<?>> primaryKeyFields,
			List<Field<?>> nonKeyFields) {
		this.tableName = tableName;
		this.primaryKeyFields = primaryKeyFields;
		this.nonKeyFields = nonKeyFields;
		this.indexes = ListTool.createArrayList();
	}
	
	public FieldSqlTableGenerator(String tableName, List<Field<?>> primaryKeyFields,
			List<Field<?>> nonKeyFields, List<List<Field<?>>> indexes) {
		this.tableName = tableName;
		this.primaryKeyFields = primaryKeyFields;
		this.nonKeyFields = nonKeyFields;
		this.indexes = indexes;
	}

	public SqlTable generate(){
		SqlTable table = new SqlTable(getTableName());
		SqlIndex pKey = new SqlIndex(getTableName() +" primary key");
		for(Field<?> f: getPrimaryKeyFields()){
			pKey.addColumn(f.getSqlColumnDefinition());
			table.addColumn(f.getSqlColumnDefinition());
		}
		table.setPrimaryKey(pKey);
		for(Field<?> f: getNonKeyFields()){
			table.addColumn(f.getSqlColumnDefinition());
		}
		int i=1;
		for(List<Field<?>> lof : indexes){
			SqlIndex index = new SqlIndex(tableName+"_index"+i);
			for(Field<?> f: lof){
				index.addColumn(f.getSqlColumnDefinition());
			}
			table.addIndex(index);
			i++;
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

	public List<List<Field<?>>> getIndexes() {
		return indexes;
	}

	public void setIndexes(List<List<Field<?>>> indexes) {
		this.indexes = indexes;
	}

}
