package com.hotpads.datarouter.op.aware;

import java.sql.Connection;

public interface ConnectionAware<T>{
	
	Connection getConnection(String clientName);
	
}
