package com.hotpads.datarouter.storage.view;

import java.util.Date;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface View<PK extends PrimaryKey<PK>> extends Databean<PK> {

//	View render();  //populate transient fields from external resources
	View<PK> deflate();  //ensure all transient fields are copied and/or encoded into persistent fields
	View<PK> inflate();  //populate all transient fields from persistent fields
	
	Date getUpdated();
	Boolean isLatest();
		
}
