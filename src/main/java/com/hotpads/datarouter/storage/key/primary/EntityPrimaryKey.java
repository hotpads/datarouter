package com.hotpads.datarouter.storage.key.primary;

import java.util.List;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.entity.EntityKey;

public interface EntityPrimaryKey<EK extends EntityKey<EK>,PK extends PrimaryKey<PK>> 
extends PrimaryKey<PK>{

	EK getEntityKey();
	List<Field<?>> getEntityKeyFields();
	List<Field<?>> getPostEntityKeyFields();
	
}