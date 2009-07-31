package com.hotpads.datarouter.app;

import java.sql.Connection;
import java.sql.SQLException;


public interface JdbcTxnApp<T> 
extends TxnApp<T> {
	
	Connection getConnection(String clientName) throws SQLException;
	
}
