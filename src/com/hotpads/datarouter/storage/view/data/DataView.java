package com.hotpads.datarouter.storage.view.data;

import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.view.View;


public interface DataView<PK extends PrimaryKey<PK>,UK extends UniqueKey<PK>> extends View<PK,UK>{

	String getData();
}
