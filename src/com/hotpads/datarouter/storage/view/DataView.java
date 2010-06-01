package com.hotpads.datarouter.storage.view;

import com.hotpads.datarouter.storage.key.primary.PrimaryKey;


public interface DataView<PK extends PrimaryKey<PK>> extends View<PK>{

	String getData();
}
