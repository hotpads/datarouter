package com.hotpads.datarouter.app;

import java.sql.Connection;



public interface ConnectionOp<T>{
	
	Connection getConnection(String clientName);
	
}
