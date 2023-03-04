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
package io.datarouter.client.memory.test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;

import io.datarouter.storage.test.node.basic.manyfield.BaseManyFieldIntegrationTests;
import io.datarouter.storage.test.node.basic.manyfield.ManyFieldBean.ManyFieldTypeBeanFielder;

@Guice(moduleFactory = DatarouterMemoryTestNgModuleFactory.class)
public class MemoryManyFieldIntegrationTests extends BaseManyFieldIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DatarouterMemoryTestClientIds.MEMORY, ManyFieldTypeBeanFielder::new);
	}

}
