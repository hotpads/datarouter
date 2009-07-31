package com.hotpads.datarouter.client.type;

import java.sql.SQLException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

public interface HibernateClient 
extends SessionClient{
	
	Session getExistingSession(String connectionName) throws SQLException;
	
	SessionFactory getSessionFactory();
	
}
