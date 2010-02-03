package com.hotpads.datarouter.client.type;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.hotpads.datarouter.connection.ConnectionHandle;

public interface HibernateClient 
extends SessionClient{
	
	Session getExistingSession();
	
	SessionFactory getSessionFactory();
	
}
