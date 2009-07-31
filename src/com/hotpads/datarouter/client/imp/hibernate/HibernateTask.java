package com.hotpads.datarouter.client.imp.hibernate;

import org.hibernate.Session;

public interface HibernateTask {

	Object run(Session session);
	
}
