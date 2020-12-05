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
package io.datarouter.secret.client.local;

import java.io.File;
import java.util.Optional;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import io.datarouter.secret.client.BaseSecretClientInternalMethodUnitTests;

public class LocalStorageSecretClientUnitTests
extends BaseSecretClientInternalMethodUnitTests<LocalStorageSecretClient>{

	private static final String FILENAME_1 = LocalStorageSecretClientUnitTests.class.getSimpleName();
	private static final String FILENAME_2 = FILENAME_1 + "2";

	@AfterMethod
	private void deleteFile(){
		new File(new TestLocalStorageConfig(FILENAME_1).getConfigFilePath()).delete();
		new File(new TestLocalStorageConfig(FILENAME_2).getConfigFilePath()).delete();
	}

	@Override
	@Test
	public void testInitialization(){
		//client1, client2, and client3 all use the same file and therefore behave identically
		LocalStorageConfig filename1 = new TestLocalStorageConfig(FILENAME_1);
		LocalStorageSecretClient client1 = new LocalStorageSecretClient(filename1);
		Assert.assertEquals(client1.listNames(Optional.empty()).size(), 0);
		LocalStorageSecretClient client2 = new LocalStorageSecretClient(filename1);
		Assert.assertEquals(client2.listNames(Optional.empty()).size(), 0);

		client1.create("name1", "");
		client2.create("name2", "");
		Assert.assertEquals(Set.copyOf(client1.listInternal(Optional.empty())), Set.of("name1", "name2"));
		Assert.assertEquals(Set.copyOf(client2.listInternal(Optional.empty())), Set.of("name1", "name2"));

		LocalStorageSecretClient client3 = new LocalStorageSecretClient(filename1);
		Assert.assertEquals(Set.copyOf(client3.listInternal(Optional.empty())), Set.of("name1", "name2"));

		//client4 uses a different file and is independent
		LocalStorageConfig filename2 = new TestLocalStorageConfig(FILENAME_2);
		LocalStorageSecretClient client4 = new LocalStorageSecretClient(filename2);
		Assert.assertEquals(client4.listInternal(Optional.empty()).size(), 0);
	}

	@Override
	protected LocalStorageSecretClient getClient(){
		var config = new TestLocalStorageConfig(FILENAME_1);
		Assert.assertFalse(new File(config.getConfigFilePath()).exists());//this gets cleaned up in deleteFile
		return new LocalStorageSecretClient(new TestLocalStorageConfig(FILENAME_1));
	}

	private static class TestLocalStorageConfig implements LocalStorageConfig{

		private final String name;

		private TestLocalStorageConfig(String name){
			this.name = name + ".properties";
		}

		@Override
		public String getConfigFilename(){
			return name;
		}

	}

}
