package com.hotpads.datarouter.op.aware;

import java.sql.Connection;

public interface ConnectionAware{
	
	//TODO replace with a generic datarouter connection interface
	Connection getConnection(String clientName);
	
}
