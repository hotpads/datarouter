package com.hotpads.datarouter.client.imp.jdbc.ddl;

public class SqlColumn {

	// ATTRIBUTES
	String name;
	MysqlColumnType Type;
	Integer MaxLength;
	Boolean nullable;
	
	public SqlColumn(String name, MysqlColumnType type, Integer maxLength,
			Boolean nullable) {
		super();
		this.name = name;
		Type = type;
		MaxLength = maxLength;
		this.nullable = nullable;
	}

	public SqlColumn(String name, MysqlColumnType type) {
		super();
		this.name = name;
		Type = type;
	}

	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MysqlColumnType getType() {
		return Type;
	}

	public void setType(MysqlColumnType type) {
		Type = type;
	}

	public Integer getMaxLength() {
		return MaxLength;
	}

	public void setMaxLength(Integer maxLength) {
		MaxLength = maxLength;
	}

	public Boolean getNullable() {
		return nullable;
	}

	public void setNullable(Boolean nullable) {
		this.nullable = nullable;
	}

	@Override
	public String toString() {
		return "SqlColumn [name=" + name + ", Type=" + Type + ", MaxLength="
				+ MaxLength + ", nullable=" + nullable + "]";
	}
	
	
	
	
}
