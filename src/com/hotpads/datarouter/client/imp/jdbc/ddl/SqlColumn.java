package com.hotpads.datarouter.client.imp.jdbc.ddl;

public class SqlColumn {

	/********************** fields *************************/
	
	protected String name;
	protected MySqlColumnType type;
	protected Integer maxLength;
	protected Boolean nullable;
	

	/********************** constructors **********************/
	
	public SqlColumn(String name, MySqlColumnType type, Integer maxLength,
			Boolean nullable) {
		this.name = name;
		this.type = type;
		this.maxLength = maxLength;
		this.nullable = nullable;
	}

	public SqlColumn(String name, MySqlColumnType type) {
		this.name = name;
		this.type = type;
	}


	/******************* Object methods **********************/
	
	@Override
	public String toString() {
		return "SqlColumn [name=" + name + ", Type=" + type + ", MaxLength="
				+ maxLength + ", nullable=" + nullable + "]";
	}

	
	/******************* methods ****************************/
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MySqlColumnType getType() {
		return type;
	}

	public void setType(MySqlColumnType type) {
		this.type = type;
	}

	public Integer getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(Integer maxLength) {
		this.maxLength = maxLength;
	}

	public Boolean getNullable() {
		return nullable;
	}

	public void setNullable(Boolean nullable) {
		this.nullable = nullable;
	}

	
	
	
	
}
