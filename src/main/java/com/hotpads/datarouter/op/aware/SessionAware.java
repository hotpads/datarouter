package com.hotpads.datarouter.op.aware;

import org.hibernate.Session;

public interface SessionAware{
	
	Session getSession(String clientName);
	
}
