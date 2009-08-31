package com.hotpads.datarouter.storage.view;

import java.util.Collection;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.storage.key.Key;

/*
 * subclasses must provide a visible no-arg constructor as they are sometimes instantiated with Class.newInstance()
 */

public abstract class ViewFactory<K extends Key<V>,V extends View>{
	protected static final Logger logger = Logger.getLogger(ViewFactory.class);
		
	public ViewFactory(){
	}

	public abstract Boolean isLatest(K key, V view);  //return null if not sure
	
	public abstract V getNew(K key);
	public abstract V getLatest(K key, boolean updateCache);
	
	public abstract V update(V view);
	
	public abstract void handleRenderSuccess(Collection<K> keys);
	public abstract void handleRenderError(Collection<K> keys);
	
}
