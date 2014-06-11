package com.hotpads.trace;

import com.hotpads.datarouter.storage.Entity.BaseEntity;
import com.hotpads.trace.key.TraceEntityKey;

public class TraceEntity extends BaseEntity<TraceEntityKey>{

	public TraceEntity(TraceEntityKey key){
		super(key);
	}

}
