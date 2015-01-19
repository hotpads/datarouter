package com.hotpads;

/**
 * Common interface to programmatically inject without knowing the implementation library (Guice, Spring...)
 * @author cguillaume
 */
public interface DatarouterInjector{

	<T>T getInstance(Class<? extends T> clazz);

}
