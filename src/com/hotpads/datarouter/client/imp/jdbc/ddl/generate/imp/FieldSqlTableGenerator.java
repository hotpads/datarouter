package com.hotpads.datarouter.client.imp.jdbc.ddl.generate.imp;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlIndex;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.SqlTableGenerator;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;

public class FieldSqlTableGenerator implements SqlTableGenerator{
	private String tableName;
	private List<Field<?>> primaryKeyFields;
	private List<Field<?>> nonKeyFields;
	private Map<String,List<Field<?>>> indexes;
	
	public FieldSqlTableGenerator(String tableName, List<Field<?>> primaryKeyFields,
			List<Field<?>> nonKeyFields) {
		this.tableName = tableName;
		this.primaryKeyFields = primaryKeyFields;
		this.nonKeyFields = nonKeyFields;
		this.indexes = MapTool.createHashMap();
	}
	
	public FieldSqlTableGenerator(String tableName, List<Field<?>> primaryKeyFields,
			List<Field<?>> nonKeyFields, Map<String,List<Field<?>>> indexes) {
		this.tableName = tableName;
		this.primaryKeyFields = primaryKeyFields;
		this.nonKeyFields = nonKeyFields;
		this.indexes = indexes;
	}

	@Override
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
		for(List<Field<?>> lof : indexes.values()){
			SqlIndex index = new SqlIndex(getKeyByValue(indexes,lof));
			for(Field<?> f: lof){
				index.addColumn(f.getSqlColumnDefinition());
			}
			table.addIndex(index);
			i++;
		}
		return table;
		
	}
	
	private String getKeyByValue(Map<String,List<Field<?>>> map, List<Field<?>> value){
		for (Entry<String, List<Field<?>>> entry : map.entrySet()) {
	        if (value.equals(entry.getValue())) {
	            return entry.getKey();
	        }
	    }
	    return null;
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

	public Map<String,List<Field<?>>> getIndexes() {
		return indexes;
	}

	public void setIndexes(Map<String,List<Field<?>>> indexes) {
		this.indexes = indexes;
	}

}
