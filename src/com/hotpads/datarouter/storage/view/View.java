package com.hotpads.datarouter.storage.view;

import java.util.Date;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;

public interface View<PK extends PrimaryKey<PK>,UK extends UniqueKey<PK>> extends Databean<PK> {

//	View render();  //populate transient fields from external resources
	View<PK,UK> deflate();  //ensure all transient fields are copied and/or encoded into persistent fields
	View<PK,UK> inflate();  //populate all transient fields from persistent fields
	
	Date getUpdated();
	Boolean isLatest();
		
}
