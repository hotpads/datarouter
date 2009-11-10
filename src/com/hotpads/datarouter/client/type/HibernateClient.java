package com.hotpads.datarouter.client.type;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

public interface HibernateClient 
extends SessionClient{
	
	Session getExistingSession(String connectionName);
	
	SessionFactory getSessionFactory();
	
}
