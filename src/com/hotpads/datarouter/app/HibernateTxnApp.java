package com.hotpads.datarouter.app;

import org.hibernate.Session;


public interface HibernateTxnApp<T> 
extends TxnApp<T> {
	
	Session getSession(String clientName);
	
}
