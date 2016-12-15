package com.hotpads.datarouter.config;

import java.sql.Connection;

import com.hotpads.datarouter.storage.field.enums.DatarouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.IntegerEnum;
import com.hotpads.datarouter.storage.field.enums.StringEnum;

public enum Isolation implements IntegerEnum<Isolation>, StringEnum<Isolation>{

	serializable(Connection.TRANSACTION_SERIALIZABLE, 20, "serializable"),
	repeatableRead(Connection.TRANSACTION_REPEATABLE_READ, 21, "repeatableRead"),
	readCommitted(Connection.TRANSACTION_READ_COMMITTED, 22, "readCommitted"),
	readUncommitted(Connection.TRANSACTION_READ_UNCOMMITTED, 23, "readUncommitted");

	public static final Isolation DEFAULT = readCommitted;


	private Integer jdbcVal;
	private int persistentInteger;
	private String persistentString;

	private Isolation(int jdbcVal, int persistentInteger, String persistentString){
		this.jdbcVal = jdbcVal;
		this.persistentInteger = persistentInteger;
		this.persistentString = persistentString;
	}


	/************* jdbc ***************/

	public Integer getJdbcVal(){
		return jdbcVal;
	}


	/***************************** IntegerEnum methods ******************************/

	@Override
	public Integer getPersistentInteger(){
		return persistentInteger;
	}

	@Override
	public Isolation fromPersistentInteger(Integer persistentInteger){
		return DatarouterEnumTool.getEnumFromInteger(values(), persistentInteger, null);
	}


	/****************************** StringEnum methods *********************************/

	@Override
	public String getPersistentString(){
		return persistentString;
	}

	@Override
	public Isolation fromPersistentString(String persistentString){
		return DatarouterEnumTool.getEnumFromString(values(), persistentString, null);
	}

}
