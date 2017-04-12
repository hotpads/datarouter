package com.hotpads.datarouter.client.imp.jdbc.ddl.domain;

public class CharSequenceSqlColumn extends SqlColumn{

	private MySqlCharacterSet characterSet;
	private MySqlCollation collation;

	public CharSequenceSqlColumn(String name, MySqlColumnType type, Integer maxLength, Boolean nullable,
			Boolean autoIncrement, String defaultValue, MySqlCharacterSet characterSet, MySqlCollation collation){
		super(name, type, maxLength, nullable, autoIncrement, defaultValue);
		this.characterSet = characterSet;
		this.collation = collation;
	}

	public MySqlCharacterSet getCharacterSet(){
		return characterSet;
	}

	public void setCharacterSet(MySqlCharacterSet characterSet){
		this.characterSet = characterSet;
	}

	public MySqlCollation getCollation(){
		return collation;
	}

	public void setCollation(MySqlCollation collation){
		this.collation = collation;
	}

	@Override
	public StringBuilder appendDataTypeDefinition(StringBuilder sb){
		return super.appendDataTypeDefinition(sb)
				.append(" character set ").append(characterSet)
				.append(" collate ").append(collation);
	}

}
