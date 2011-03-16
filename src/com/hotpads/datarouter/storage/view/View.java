package com.hotpads.datarouter.storage.view;

import java.util.Date;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;

public interface View<
		VK extends PrimaryKey<VK>,
		V extends Databean<VK,V>,
		UK extends UniqueKey<VK>> 
extends Databean<VK,V>{

//	View render();  //populate transient fields from external resources
	View<VK,V,UK> deflate();  //ensure all transient fields are copied and/or encoded into persistent fields
	View<VK,V,UK> inflate();  //populate all transient fields from persistent fields
	
	Date getUpdated();
	Boolean isLatest();
}
