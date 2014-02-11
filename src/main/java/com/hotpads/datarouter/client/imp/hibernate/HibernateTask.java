package com.hotpads.datarouter.client.imp.hibernate;

import org.hibernate.Session;

@Deprecated//replace with HibernateOps
public interface HibernateTask {

	Object run(Session session);
	
}
