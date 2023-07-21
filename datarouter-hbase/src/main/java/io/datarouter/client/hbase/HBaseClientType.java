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
package io.datarouter.client.hbase;

import io.datarouter.client.hbase.web.HBaseWebInspector;
import io.datarouter.storage.client.ClientType;
import io.datarouter.web.browse.DatarouterClientWebInspectorRegistry;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class HBaseClientType implements ClientType<HBaseClientNodeFactory,HBaseClientManager>{

	public static final String NAME = "hbase";
	public static final String NAME_BIG_TABLE = "bigtable";

	@Inject
	public HBaseClientType(DatarouterClientWebInspectorRegistry datarouterClientWebInspectorRegistry){
		datarouterClientWebInspectorRegistry.register(NAME, HBaseWebInspector.class);
	}

	@Override
	public String getName(){
		return NAME;
	}

	@Override
	public Class<HBaseClientNodeFactory> getClientNodeFactoryClass(){
		return HBaseClientNodeFactory.class;
	}

	@Override
	public Class<HBaseClientManager> getClientManagerClass(){
		return HBaseClientManager.class;
	}

	public static boolean isBigTable(ClientType<?,?> clientType){
		return NAME_BIG_TABLE.equals(clientType.getName());
	}

}
