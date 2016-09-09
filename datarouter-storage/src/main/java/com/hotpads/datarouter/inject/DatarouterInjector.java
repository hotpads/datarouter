package com.hotpads.datarouter.inject;

import java.util.Map;


/**
 * Common interface to programmatically inject without knowing the implementation library (Guice, Spring...)
 * @author cguillaume
 */
public interface DatarouterInjector{

	<T> T getInstance(Class<? extends T> clazz);

	<T> Map<String,T> getInstancesOfType(Class<T> type);

	void injectMenbers(Object instance);

}
