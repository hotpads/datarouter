package com.hotpads.datarouter.app;

import org.hibernate.Session;



public interface SessionOp<T>{
	
	Session getSession(String clientName);
	
}
