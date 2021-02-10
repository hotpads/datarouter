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
package io.datarouter.secret.client.memory;

import java.util.Map;
import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.secret.client.BaseSecretClientInternalMethodUnitTests;

public class MemorySecretClientUnitTests extends BaseSecretClientInternalMethodUnitTests<MemorySecretClient>{

	@Override
	@Test
	public void testInitialization(){
		MemorySecretClient client = new MemorySecretClient();
		Assert.assertEquals(client.listNames(Optional.empty()).result.get().size(), 0);
		client = new MemorySecretClient(Map.of());
		Assert.assertEquals(client.listNames(Optional.empty()).result.get().size(), 0);
		client = new MemorySecretClient(Map.of("key", "value"));
		Assert.assertEquals(client.listNames(Optional.empty()).result.get().size(), 1);
	}

	@Override
	protected MemorySecretClient getClient(){
		return new MemorySecretClient();
	}

}
