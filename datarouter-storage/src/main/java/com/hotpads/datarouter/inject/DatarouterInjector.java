package com.hotpads.datarouter.inject;

import java.util.Collection;


/**
 * Common interface to programmatically inject without knowing the implementation library (Guice, Spring...)
 * @author cguillaume
 */
public interface DatarouterInjector{

	<T> T getInstance(Class<? extends T> clazz);

	<T> Collection<T> getInstancesOfType(Class<T> type);

	void injectMenbers(Object instance);

}
