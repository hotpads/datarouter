package com.hotpads.datarouter.storage.view.data;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.view.View;


public interface DataView<
		VK extends PrimaryKey<VK>,
		V extends Databean<VK,V>,
		UK extends UniqueKey<VK>> 
extends View<VK,V,UK>{

	String getData();
}
