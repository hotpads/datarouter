package com.hotpads.datarouter.client.imp.hibernate.client;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.hotpads.datarouter.client.type.JdbcClient;

public interface HibernateClient 
extends JdbcClient{
	
	Session getExistingSession();
	
	SessionFactory getSessionFactory();
	
}
