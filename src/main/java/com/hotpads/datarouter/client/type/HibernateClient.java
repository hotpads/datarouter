package com.hotpads.datarouter.client.type;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

public interface HibernateClient 
extends JdbcClient{
	
	Session getExistingSession();
	
	SessionFactory getSessionFactory();
	
}
