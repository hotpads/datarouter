package com.hotpads.datarouter.client.imp.jdbc.ddl.generate.imp;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCharacterSet;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCollation;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlIndex;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.SqlTableGenerator;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.util.core.MapTool;

public class FieldSqlTableGenerator implements SqlTableGenerator{
	
	private String tableName;
	private List<Field<?>> primaryKeyFields;
	private List<Field<?>> nonKeyFields;
	private Map<String,List<Field<?>>> indexes;
	private MySqlCollation collation = SqlTable.getDefaultCollation();
	private MySqlCharacterSet character_set = SqlTable.getDefaultCharacterSet();

	
	public FieldSqlTableGenerator(String tableName, List<Field<?>> primaryKeyFields, List<Field<?>> nonKeyFields){
		this.tableName = tableName;
		this.nonKeyFields = nonKeyFields;
		this.indexes = MapTool.createHashMap();
		this.primaryKeyFields = primaryKeyFields;
	}
	

	public FieldSqlTableGenerator(String tableName, List<Field<?>> primaryKeyFields, List<Field<?>> nonKeyFields,
			MySqlCollation collation, MySqlCharacterSet character_set){
		this.tableName = tableName;
		this.nonKeyFields = nonKeyFields;
		this.indexes = MapTool.createHashMap();
		this.primaryKeyFields = primaryKeyFields;
		this.collation = collation;
		this.character_set = character_set;
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
		for(List<Field<?>> listOfFields : indexes.values()){
			SqlIndex index = new SqlIndex(getKeyByValue(indexes, listOfFields));
			for(Field<?> field : listOfFields){
				index.addColumn(field.getSqlColumnDefinition());
			}
			table.addIndex(index);
		}
		table.setCharSet(character_set);
		table.setCollation(collation);
		return table;
		
	}
	
	private String getKeyByValue(Map<String,List<Field<?>>> map, List<Field<?>> value){
		for (Entry<String, List<Field<?>>> entry : map.entrySet()){
	        if (value.equals(entry.getValue())){
	            return entry.getKey();
	        }
	    }
	    return null;
	}

	
	/*************************** get/set *************************************/
	
	public String getTableName(){
		return tableName;
	}

	public void setTableName(String tableName){
		this.tableName = tableName;
	}

	public List<Field<?>> getPrimaryKeyFields(){
		return primaryKeyFields;
	}

	public void setPrimaryKeyFields(List<Field<?>> primaryKeyFields){
		this.primaryKeyFields = primaryKeyFields;
	}

	public List<Field<?>> getNonKeyFields(){
		return nonKeyFields;
	}

	public void setNonKeyFields(List<Field<?>> nonKeyFields){
		this.nonKeyFields = nonKeyFields;
	}

	public Map<String,List<Field<?>>> getIndexes(){
		return indexes;
	}

	public void setIndexes(Map<String,List<Field<?>>> indexes){
		this.indexes = indexes;
	}

}
