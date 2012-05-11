package com.hotpads.datarouter.client.imp.jdbc.ddl;

import static org.junit.Assert.fail;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.hotpads.datarouter.node.Nodes;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.comparable.BooleanField;
import com.hotpads.datarouter.storage.field.imp.positive.UInt15Field;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBean;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;

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
			table.addColumn(f.getSqlColumnDefinition());
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
