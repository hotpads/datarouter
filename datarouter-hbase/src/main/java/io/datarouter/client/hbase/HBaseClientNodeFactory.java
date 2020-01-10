/**
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

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.hbase.callback.CountingBatchCallbackFactory;
import io.datarouter.client.hbase.config.DatarouterHBaseExecutors.DatarouterHbaseClientExecutor;
import io.datarouter.storage.node.adapter.availability.PhysicalSortedMapStorageAvailabilityAdapterFactory;
import io.datarouter.storage.node.adapter.availability.PhysicalSubEntitySortedMapStorageAvailabilityAdapterFactory;

@Singleton
public class HBaseClientNodeFactory extends BaseHBaseClientNodeFactory{

	@Inject
	public HBaseClientNodeFactory(
			HBaseClientType clientType,
			PhysicalSortedMapStorageAvailabilityAdapterFactory physicalSortedMapStorageAvailabilityAdapterFactory,
			PhysicalSubEntitySortedMapStorageAvailabilityAdapterFactory
			physicalSubEntitySortedMapStorageAvailabilityAdapterFactory,
			CountingBatchCallbackFactory countingBatchCallbackFactory,
			HBaseClientManager hBaseClientManager,
			DatarouterHbaseClientExecutor datarouterHbaseClientExecutor){
		super(clientType, physicalSortedMapStorageAvailabilityAdapterFactory,
				physicalSubEntitySortedMapStorageAvailabilityAdapterFactory, countingBatchCallbackFactory,
				hBaseClientManager, datarouterHbaseClientExecutor);
	}

}
