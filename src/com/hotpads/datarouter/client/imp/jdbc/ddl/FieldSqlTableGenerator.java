package com.hotpads.datarouter.client.imp.jdbc.ddl;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;

public class FieldSqlTableGenerator {

	private List<Field<?>> primaryKeyFields;
	private List<Field<?>> nonKeyFields;
	
	public FieldSqlTableGenerator(List<Field<?>> primaryKeyFields,
			List<Field<?>> nonKeyFields) {
		this.primaryKeyFields = primaryKeyFields;
		this.nonKeyFields = nonKeyFields;
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
