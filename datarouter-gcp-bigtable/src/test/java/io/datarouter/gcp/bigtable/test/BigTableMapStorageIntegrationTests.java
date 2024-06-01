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
package io.datarouter.gcp.bigtable.test;

import java.util.Optional;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;

import io.datarouter.gcp.bigtable.config.DatarouterBigtableTestNgModuleFactory;
import io.datarouter.storage.test.node.basic.map.BaseMapStorageIntegrationTests;

@Guice(moduleFactory = DatarouterBigtableTestNgModuleFactory.class)
public class BigtableMapStorageIntegrationTests extends BaseMapStorageIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DatarouterBigtableTestClientIds.BIG_TABLE, Optional.of("MapStorageBigtableNative"));
	}

}
