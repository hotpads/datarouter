package com.hotpads.datarouter.storage.view;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.status.StandardStatus;

/*
 * subclasses must provide a visible no-arg constructor as they are sometimes instantiated with Class.newInstance()
 */

public abstract class ViewFactory<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		UK extends UniqueKey<PK>,
		V extends View<PK,D,UK>>{
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
		
	public ViewFactory(){
	}

	public abstract Boolean isLatest(UK key, V view, boolean ignoreVersion);  //return null if not sure
	
	public abstract V getNew(UK key);
	public abstract V getLatest(UK key, boolean updateCache, boolean ignoreVersion);
	
	public abstract V update(V view);
	
	public abstract void handleRenderOutcome(Collection<UK> keys, StandardStatus newStatus);
	
}
