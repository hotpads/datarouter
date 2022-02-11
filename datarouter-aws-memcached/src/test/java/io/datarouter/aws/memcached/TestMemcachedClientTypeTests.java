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
package io.datarouter.aws.memcached;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.aws.memcached.test.DatarouterAwsMemcachedModuleFactory;
import io.datarouter.storage.client.ClientTypeRegistry;

@Guice(moduleFactory = DatarouterAwsMemcachedModuleFactory.class)
public class TestMemcachedClientTypeTests{

	@Inject
	private ClientTypeRegistry clientTypeRegistry;

	@Test
	public void testClassLocation(){
		Assert.assertEquals(
				clientTypeRegistry.get(TestMemcachedClientType.NAME).getClass(),
				TestMemcachedClientType.class);
	}

}