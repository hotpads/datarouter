package com.hotpads.datarouter.client.imp.jdbc.ddl;

public class SqlColumn {

	// ATTRIBUTES
	String name;
	MySqlColumnType type;
	Integer maxLength;
	Boolean nullable;
	
	public SqlColumn(String name, MySqlColumnType type, Integer maxLength,
			Boolean nullable) {
		super();
		this.name = name;
		this.type = type;
		this.maxLength = maxLength;
		this.nullable = nullable;
	}

	public SqlColumn(String name, MySqlColumnType type) {
		super();
		this.name = name;
		this.type = type;
	}

	
	
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

	public int getMaxLength() {
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

	@Override
	public String toString() {
		return "SqlColumn [name=" + name + ", Type=" + type + ", MaxLength="
				+ maxLength + ", nullable=" + nullable + "]";
	}
	
	
	
	
}
