package com.hotpads.datarouter.node.type.writebehind.mixin;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class PutWrapper<PK extends PrimaryKey<PK>, D extends Databean<PK, D>> {

	private Collection<D> databeans;
	private Config config;

	public PutWrapper(Collection<D> databeans, Config config) {
		this.databeans = databeans;
		this.config = config;
	}

	public Collection<D> getDatabeans() {
		return databeans;
	}

	public Config getConfig() {
		return config;
	}

}
