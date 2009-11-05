package com.hotpads.datarouter.config;

public enum PutMethod {

	selectThenDecide,  //"pessimistic", slow but sure
	updateOrInsert,  //"optimistic" when rows are usually there (Events use this)
	insertOrUpdate,  //overwrites whatever's there (pessimistic)
	insertOrBust,
	updateOrBust;
	
}
