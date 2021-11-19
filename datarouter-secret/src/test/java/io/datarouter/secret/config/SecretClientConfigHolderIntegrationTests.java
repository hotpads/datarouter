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
package io.datarouter.secret.config;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.scanner.Scanner;
import io.datarouter.secret.client.SecretClient.SecretClientSupplier;
import io.datarouter.secret.client.local.LocalStorageSecretClientSupplier;
import io.datarouter.secret.client.memory.MemorySecretClientSupplier;
import io.datarouter.secret.op.SecretOp;
import io.datarouter.secret.op.client.SecretClientOpType;

public class SecretClientConfigHolderIntegrationTests{

	private static final SecretOp<?,?,?,?> READ_OP = SecretClientConfigUnitTests.emptyOpInfo(SecretClientOpType.READ);
	private static final SecretOp<?,?,?,?> WRITE_OP = SecretClientConfigUnitTests.emptyOpInfo(
			SecretClientOpType.CREATE);

	private static final List<Class<? extends SecretClientSupplier>> ONE_CLASSES = List.of(
			MemorySecretClientSupplier.class);
	private static final List<SecretClientSupplierConfig> ONE_CONFIG = Scanner.of(ONE_CLASSES)
			.map(SecretClientConfigHolderIntegrationTests::makeConfig)
			.list();

	private static final List<Class<? extends SecretClientSupplier>> TWO_CLASSES = List.of(
			MemorySecretClientSupplier.class,
			LocalStorageSecretClientSupplier.class);
	private static final List<SecretClientSupplierConfig> TWO_CONFIG = Scanner.of(TWO_CLASSES)
			.map(SecretClientConfigHolderIntegrationTests::makeConfig)
			.list();

	@Test
	public void testInit(){
		SecretClientSupplierConfigHolder empty = new SecretClientSupplierConfigHolder();
		assertConfigsContainClasses(empty.getAllowedConfigs(true, READ_OP), List.of());
		assertConfigsContainClasses(empty.getAllowedConfigs(false, READ_OP), List.of());

		SecretClientSupplierConfigHolder singleList = new SecretClientSupplierConfigHolder(ONE_CONFIG);
		assertConfigsContainClasses(singleList.getAllowedConfigs(true, READ_OP), ONE_CLASSES);
		assertConfigsContainClasses(singleList.getAllowedConfigs(false, READ_OP), ONE_CLASSES);

		SecretClientSupplierConfigHolder separateLists = new SecretClientSupplierConfigHolder(ONE_CONFIG, TWO_CONFIG);
		assertConfigsContainClasses(separateLists.getAllowedConfigs(true, READ_OP), ONE_CLASSES);
		assertConfigsContainClasses(separateLists.getAllowedConfigs(false, READ_OP), TWO_CLASSES);
	}

	//NOTE: this is meant to test results of SecretClientConfigHolder#getAllowedSecretClientSupplierClasses, not the
	//possibilities of SecretClientConfig#allowed, which has its own tests in SecretClientConfigUnitTests
	@Test
	public void testAllowed(){
		SecretClientSupplierConfigHolder readWrite = new SecretClientSupplierConfigHolder(List.of(
				SecretClientSupplierConfig.readOnly("TEST", MemorySecretClientSupplier.class),
				SecretClientSupplierConfig.allOps("TEST", LocalStorageSecretClientSupplier.class)));

		assertConfigsContainClasses(readWrite.getAllowedConfigs(true, READ_OP), TWO_CLASSES);
		assertConfigsContainClasses(
				readWrite.getAllowedConfigs(true, WRITE_OP),
				List.of(LocalStorageSecretClientSupplier.class));


		SecretClientSupplierConfigHolder writeRead = new SecretClientSupplierConfigHolder(List.of(
				SecretClientSupplierConfig.allOps("TEST", MemorySecretClientSupplier.class),
				SecretClientSupplierConfig.readOnly("TEST", LocalStorageSecretClientSupplier.class)));
		assertConfigsContainClasses(writeRead.getAllowedConfigs(true, READ_OP), TWO_CLASSES);
		assertConfigsContainClasses(
				writeRead.getAllowedConfigs(true, WRITE_OP),
				List.of(MemorySecretClientSupplier.class));
	}

	private static SecretClientSupplierConfig makeConfig(Class<? extends SecretClientSupplier> cls){
		return SecretClientSupplierConfig.allOps("TEST", cls);
	}

	private static void assertConfigsContainClasses(Scanner<SecretClientSupplierConfig> secretClientConfigScanner,
			List<Class<? extends SecretClientSupplier>> expected){
		Assert.assertEquals(
				secretClientConfigScanner.map(SecretClientSupplierConfig::getSecretClientSupplierClass).list(),
				expected);
	}

}
