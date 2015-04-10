package com.hotpads.datarouter.client.imp.hibernate.op;

import org.hibernate.Session;

public interface SessionAware{
	
	Session getSession(String clientName);
	
}
