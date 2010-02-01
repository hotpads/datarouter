package com.hotpads.datarouter.app;

import java.sql.Connection;


public interface JdbcTxnApp<T> 
extends TxnApp<T> {
	
	Connection getConnection(String clientName);
	
}
