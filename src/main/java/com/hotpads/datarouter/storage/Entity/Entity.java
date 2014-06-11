package com.hotpads.datarouter.storage.Entity;

import com.hotpads.datarouter.storage.key.entity.EntityKey;

public interface Entity<EK extends EntityKey<EK>>{

	EK getKey();

}
