package com.hotpads.datarouter.storage.databean;

import org.hibernate.mapping.RootClass;

public interface HibernateConfig {
	RootClass getRootClass(String tableName);
}
