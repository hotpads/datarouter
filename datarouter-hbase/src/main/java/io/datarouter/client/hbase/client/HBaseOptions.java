/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.client.hbase.client;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.client.ClientOptions;

@Singleton
public class HBaseOptions{

	private static final String PREFIX_hbase = "hbase.";

	protected static final String PROP_zookeeperQuorum = "zookeeper.quorum";
	protected static final String PROP_maxHTables = "maxHTables";
	protected static final String PROP_minThreadsPerHTable = "minThreadsPerHTable";
	protected static final String PROP_maxThreadsPerHTable = "maxThreadsPerHTable";
	protected static final String PROP_minPoolSize = "minPoolSize";

	@Inject
	private ClientOptions clientOptions;

	public String zookeeperQuorum(String clientName){
		return clientOptions.getRequiredString(clientName, makeHbaseKey(PROP_zookeeperQuorum));
	}

	public Integer maxHTables(String clientName, int def){
		return clientOptions.optString(clientName, makeHbaseKey(PROP_maxHTables))
				.map(Integer::valueOf)
				.orElse(def);
	}

	public Integer minThreadsPerHTable(String clientName, int def){
		return clientOptions.optString(clientName, makeHbaseKey(PROP_minThreadsPerHTable))
				.map(Integer::valueOf)
				.orElse(def);
	}

	public Integer maxThreadsPerHTable(String clientName, int def){
		return clientOptions.optString(clientName, makeHbaseKey(PROP_maxThreadsPerHTable))
				.map(Integer::valueOf)
				.orElse(def);
	}

	protected static String makeHbaseKey(String propertyKey){
		return PREFIX_hbase + propertyKey;
	}

}
