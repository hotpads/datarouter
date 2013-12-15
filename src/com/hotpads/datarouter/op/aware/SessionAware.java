package com.hotpads.datarouter.op.aware;

import org.hibernate.Session;



public interface SessionAware<T>{
	
	Session getSession(String clientName);
	
}
