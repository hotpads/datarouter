package com.hotpads.datarouter.config;

import java.util.Set;

import com.hotpads.util.core.SetTool;

public enum PutMethod {

	selectFirstOrLookAtPrimaryKey,   //"pessimistic", slow but sure
	updateOrInsert,  //"optimistic" when rows are usually there (Events use this)
	insertOrUpdate,  // will overwrite whatever's there 
	insertOrBust,
	updateOrBust;
	
	//need to flush immediately so we can catch insert/update exceptions if they are thrown, otherwise
	//   the exception will ruin the whole batch
	public static Set<PutMethod> methodsToFlushImmediately = SetTool.createHashSet();
	static{
		methodsToFlushImmediately.add(updateOrInsert);
		methodsToFlushImmediately.add(insertOrUpdate);
	}
	
}
