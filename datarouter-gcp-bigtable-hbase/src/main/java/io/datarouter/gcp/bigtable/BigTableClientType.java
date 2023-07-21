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
package io.datarouter.gcp.bigtable;

import io.datarouter.client.hbase.HBaseClientType;
import io.datarouter.gcp.bigtable.client.BigTableClientManager;
import io.datarouter.gcp.bigtable.web.BigTableWebInspector;
import io.datarouter.storage.client.ClientType;
import io.datarouter.web.browse.DatarouterClientWebInspectorRegistry;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class BigTableClientType implements ClientType<BigTableClientNodeFactory,BigTableClientManager>{

	public static final String NAME = HBaseClientType.NAME_BIG_TABLE;

	@Inject
	public BigTableClientType(DatarouterClientWebInspectorRegistry datarouterClientWebInspectorRegistry){
		datarouterClientWebInspectorRegistry.register(NAME, BigTableWebInspector.class);
	}

	@Override
	public String getName(){
		return NAME;
	}

	@Override
	public Class<BigTableClientNodeFactory> getClientNodeFactoryClass(){
		return BigTableClientNodeFactory.class;
	}

	@Override
	public Class<BigTableClientManager> getClientManagerClass(){
		return BigTableClientManager.class;
	}

}
