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
package io.datarouter.secret.client;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.secret.exception.SecretExistsException;
import io.datarouter.secret.exception.SecretNotFoundException;

public abstract class BaseSecretClientInternalMethodUnitTests<T extends SecretClient>{

	protected abstract T getClient();

	@Test
	public abstract void testInitialization();

	@Test
	public void testCrudInternal(){
		T client = getClient();

		//create
		client.create(new Secret("name1", "value1"));
		Assert.assertThrows(SecretExistsException.class, () -> client.create(new Secret("name1", "value1")));

		//read
		Assert.assertEquals(client.read("name1").getValue(), "value1");
		Assert.assertThrows(SecretNotFoundException.class, () -> client.read("name2"));

		//update
		Assert.assertThrows(SecretNotFoundException.class, () -> client.update(new Secret("name2", "value1")));
		client.update(new Secret("name1","value2"));
		Assert.assertEquals(client.read("name1").getValue(), "value2");

		//delete
		Assert.assertThrows(SecretNotFoundException.class, () -> client.delete("name2"));
		client.delete("name1");
		Assert.assertThrows(SecretNotFoundException.class, () -> client.read("name1"));
	}

	@Test
	public void testListNames(){
		T client = getClient();

		client.create("name1", "");
		Assert.assertEquals(client.listNames(Optional.empty()), List.of("name1"));

		client.create("name2", "");
		client.create("other", "");
		client.create("first", "");
		client.create("no", "");
		Assert.assertEquals(Set.copyOf(client.listNames(Optional.empty())), Set.of("first", "name1", "name2", "no",
				"other"));

		//prefix
		Assert.assertEquals(client.listNames(Optional.of("missing")).size(), 0);
		Assert.assertEquals(Set.copyOf(client.listNames(Optional.of("n"))), Set.of("name1", "name2", "no"));
		Assert.assertEquals(Set.copyOf(client.listNames(Optional.of("name"))), Set.of("name1", "name2"));
	}

}
