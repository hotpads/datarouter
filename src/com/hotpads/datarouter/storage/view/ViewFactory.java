package com.hotpads.datarouter.storage.view;

import java.util.Collection;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;

/*
 * subclasses must provide a visible no-arg constructor as they are sometimes instantiated with Class.newInstance()
 */

public abstract class ViewFactory<PK extends PrimaryKey<PK>,UK extends UniqueKey<PK>,V extends View<PK,UK>>{
	protected final Logger logger = Logger.getLogger(this.getClass());
		
	public ViewFactory(){
	}

	public abstract Boolean isLatest(UK key, V view);  //return null if not sure
	
	public abstract V getNew(UK key);
	public abstract V getLatest(UK key, boolean updateCache);
	
	public abstract V update(V view);
	
	public abstract void handleRenderSuccess(Collection<UK> keys);
	public abstract void handleRenderError(Collection<UK> keys);
	
}
