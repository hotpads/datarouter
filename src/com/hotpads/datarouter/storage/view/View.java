package com.hotpads.datarouter.storage.view;

import java.util.Date;

import com.hotpads.datarouter.storage.databean.Databean;

public interface View extends Databean {

//	View render();  //populate transient fields from external resources
	View deflate();  //ensure all transient fields are copied and/or encoded into persistent fields
	View inflate();  //populate all transient fields from persistent fields
	
	Date getUpdated();
	Boolean isLatest();
	
}
