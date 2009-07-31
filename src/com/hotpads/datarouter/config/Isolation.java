package com.hotpads.datarouter.config;

import java.sql.Connection;

public enum Isolation {

	serializable(Connection.TRANSACTION_SERIALIZABLE),
	repeatableRead(Connection.TRANSACTION_REPEATABLE_READ),
	readCommitted(Connection.TRANSACTION_READ_COMMITTED),
	readUncommitted(Connection.TRANSACTION_READ_UNCOMMITTED);
	
	private Integer jdbcVal;
	
	Isolation(int jdbcVal){
		this.jdbcVal = jdbcVal;
	}

	public Integer getJdbcVal() {
		return jdbcVal;
	}
	
	
}
