package com.hotpads.datarouter.config;

import java.util.Set;

import com.hotpads.util.core.SetTool;

public enum PutMethod {

	SELECT_FIRST_OR_LOOK_AT_PRIMARY_KEY,   //"pessimistic", slow but sure
	UPDATE_OR_INSERT,  //"optimistic" when rows are usually there (Events use this)
	INSERT_OR_UPDATE,  // will overwrite whatever's there 
	INSERT_OR_BUST,
	UPDATE_OR_BUST,
	MERGE;//use when the object could be on the session already in a different instance with the same identifier
	
	//need to flush immediately so we can catch insert/update exceptions if they are thrown, 
	//   otherwise the exception will ruin the whole batch
	public static Set<PutMethod> METHODS_TO_FLUSH_IMMEDIATELY = SetTool.createHashSet();
	static{
		METHODS_TO_FLUSH_IMMEDIATELY.add(UPDATE_OR_INSERT);
		METHODS_TO_FLUSH_IMMEDIATELY.add(INSERT_OR_UPDATE);
	}
	
}
