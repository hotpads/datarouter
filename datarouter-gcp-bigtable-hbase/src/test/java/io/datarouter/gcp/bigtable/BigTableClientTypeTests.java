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

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.gcp.bigtable.config.DatarouterBigTableTestNgModuleFactory;
import io.datarouter.storage.client.ClientTypeRegistry;
import jakarta.inject.Inject;

@Guice(moduleFactory = DatarouterBigTableTestNgModuleFactory.class)
public class BigTableClientTypeTests{

	@Inject
	private ClientTypeRegistry clientTypeRegistry;

	@Test
	public void testClassLocation(){
		Assert.assertEquals(BigTableClientType.class, clientTypeRegistry.get(BigTableClientType.NAME).getClass());
	}

}
