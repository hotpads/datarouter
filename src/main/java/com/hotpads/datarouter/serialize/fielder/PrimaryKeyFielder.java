package com.hotpads.datarouter.serialize.fielder;

import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface PrimaryKeyFielder<PK extends PrimaryKey<PK>>
extends Fielder<PK>{

	boolean isEntity();

}
