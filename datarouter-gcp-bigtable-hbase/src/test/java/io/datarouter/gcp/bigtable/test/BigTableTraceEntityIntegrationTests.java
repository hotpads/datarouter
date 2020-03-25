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
package io.datarouter.gcp.bigtable.test;

import javax.inject.Inject;

import org.testng.annotations.Guice;

import io.datarouter.client.hbase.test.BaseTraceEntityIntegrationTests;
import io.datarouter.gcp.bigtable.config.DatarouterBigTableTestNgModuleFactory;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.node.factory.EntityNodeFactory;
import io.datarouter.storage.node.factory.WideNodeFactory;

@Guice(moduleFactory = DatarouterBigTableTestNgModuleFactory.class)
public class BigTableTraceEntityIntegrationTests extends BaseTraceEntityIntegrationTests{

	@Inject
	public BigTableTraceEntityIntegrationTests(
			Datarouter datarouter,
			EntityNodeFactory entityNodeFactory,
			WideNodeFactory wideNodeFactory){
		super(datarouter, entityNodeFactory, wideNodeFactory, DatarouterBigTableTestClientIds.BIG_TABLE);
	}

}
