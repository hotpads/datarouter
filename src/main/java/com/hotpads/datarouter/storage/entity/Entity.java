package com.hotpads.datarouter.storage.entity;

import java.util.NavigableMap;

import com.hotpads.datarouter.storage.entity.BaseEntity.EntitySection;
import com.hotpads.datarouter.storage.key.entity.EntityKey;

public interface Entity<EK extends EntityKey<EK>>{

	EK getKey();
	NavigableMap<String,EntitySection<EK,?,?>> getDatabeansByNodeName();
}
