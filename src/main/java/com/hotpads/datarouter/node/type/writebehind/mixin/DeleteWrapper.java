package com.hotpads.datarouter.node.type.writebehind.mixin;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class DeleteWrapper<PK extends PrimaryKey<PK>> {

	private Collection<PK> keys;
	private Config config;

	public DeleteWrapper(Collection<PK> keys, Config config) {
		this.keys = keys;
		this.config = config;
	}

	public Collection<PK> getKeys() {
		return keys;
	}

	public Config getConfig() {
		return config;
	}

}
