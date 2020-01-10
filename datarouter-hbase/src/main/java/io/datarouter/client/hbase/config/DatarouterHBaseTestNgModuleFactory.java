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
package io.datarouter.client.hbase.config;

import java.util.Arrays;

import io.datarouter.inject.testng.TestNgModuleFactory;
import io.datarouter.storage.config.guice.DatarouterStorageTestGuiceModule;
import io.datarouter.web.config.DatarouterWebTestGuiceModule;

public class DatarouterHBaseTestNgModuleFactory extends TestNgModuleFactory{

	public DatarouterHBaseTestNgModuleFactory(){
		super(Arrays.asList(
				new DatarouterWebTestGuiceModule(),
				new DatarouterHBaseTestGuiceModule(),
				new DatarouterStorageTestGuiceModule()));
	}

}
