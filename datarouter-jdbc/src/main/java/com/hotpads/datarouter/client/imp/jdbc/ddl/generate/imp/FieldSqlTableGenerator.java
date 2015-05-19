package com.hotpads.datarouter.client.imp.jdbc.ddl.generate.imp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCharacterSet;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCollation;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlIndex;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.SqlTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.field.JdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.storage.field.Field;

public class FieldSqlTableGenerator implements SqlTableGenerator{
	
	private final JdbcFieldCodecFactory fieldCodecFactory;
	private final String tableName;
	private final List<Field<?>> primaryKeyFields;
	private final List<Field<?>> nonKeyFields;
	private Map<String,List<Field<?>>> indexes;
	private final MySqlCollation collation;
	private final MySqlCharacterSet character_set;

	
	public FieldSqlTableGenerator(JdbcFieldCodecFactory fieldCodecFactory, String tableName, 
			List<Field<?>> primaryKeyFields, List<Field<?>> nonKeyFields){
		this(fieldCodecFactory, tableName, primaryKeyFields, nonKeyFields, SqlTable.getDefaultCollation(), 
				SqlTable.getDefaultCharacterSet());
	}
	

	public FieldSqlTableGenerator(JdbcFieldCodecFactory fieldCodecFactory, String tableName, 
			List<Field<?>> primaryKeyFields, List<Field<?>> nonKeyFields,
			MySqlCollation collation, MySqlCharacterSet character_set){
		this.fieldCodecFactory = fieldCodecFactory;
		this.tableName = tableName;
		this.nonKeyFields = nonKeyFields;
		this.indexes = new HashMap<>();
		this.primaryKeyFields = primaryKeyFields;
		this.collation = collation;
		this.character_set = character_set;
	}


	@Override
	public SqlTable generate(){
		SqlTable table = new SqlTable(getTableName());
		SqlIndex pKey = new SqlIndex(getTableName() +" primary key");
		for(JdbcFieldCodec<?,?> codec : fieldCodecFactory.createCodecs(getPrimaryKeyFields())){
			pKey.addColumn(codec.getSqlColumnDefinition());
			table.addColumn(codec.getSqlColumnDefinition());
		}
		table.setPrimaryKey(pKey);
		for(JdbcFieldCodec<?,?> codec : fieldCodecFactory.createCodecs(getNonKeyFields())){
			table.addColumn(codec.getSqlColumnDefinition());
		}
		for(List<Field<?>> listOfFields : indexes.values()){
			SqlIndex index = new SqlIndex(getKeyByValue(indexes, listOfFields));
			for(JdbcFieldCodec<?,?> codec : fieldCodecFactory.createCodecs(listOfFields)){
				index.addColumn(codec.getSqlColumnDefinition());
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

	public List<Field<?>> getPrimaryKeyFields(){
		return primaryKeyFields;
	}

	public List<Field<?>> getNonKeyFields(){
		return nonKeyFields;
	}

	public Map<String,List<Field<?>>> getIndexes(){
		return indexes;
	}

	public void setIndexes(Map<String,List<Field<?>>> indexes){
		this.indexes = indexes;
	}

}
