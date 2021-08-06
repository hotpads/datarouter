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
import io.datarouter.secret.op.SecretOpInfo;
import io.datarouter.secret.op.SecretOpReason;
import io.datarouter.secret.op.SecretOpType;

public class SecretClientConfigHolderIntegrationTests{

	private static final SecretOpInfo READ_OP = new SecretOpInfo(SecretOpType.READ, "", "", SecretOpReason.automatedOp(
			SecretClientConfigHolderIntegrationTests.class.getSimpleName()));
	private static final SecretOpInfo WRITE_OP = new SecretOpInfo(SecretOpType.CREATE, "", "", SecretOpReason
			.automatedOp(SecretClientConfigHolderIntegrationTests.class.getSimpleName()));

	private static final List<Class<? extends SecretClientSupplier>> ONE_CLASSES = List.of(
			MemorySecretClientSupplier.class);
	private static final List<SecretClientConfig> ONE_CONFIG = Scanner.of(ONE_CLASSES)
			.map(SecretClientConfigHolderIntegrationTests::makeConfig)
			.list();

	private static final List<Class<? extends SecretClientSupplier>> TWO_CLASSES = List.of(
			MemorySecretClientSupplier.class,
			LocalStorageSecretClientSupplier.class);
	private static final List<SecretClientConfig> TWO_CONFIG = Scanner.of(TWO_CLASSES)
			.map(SecretClientConfigHolderIntegrationTests::makeConfig)
			.list();

	@Test
	public void testInit(){
		SecretClientConfigHolder empty = new SecretClientConfigHolder();
		assertConfigsContainClasses(empty.getAllowedSecretClientConfigs(true, READ_OP), List.of());
		assertConfigsContainClasses(empty.getAllowedSecretClientConfigs(false, READ_OP), List.of());

		SecretClientConfigHolder singleList = new SecretClientConfigHolder(ONE_CONFIG);
		assertConfigsContainClasses(singleList.getAllowedSecretClientConfigs(true, READ_OP), ONE_CLASSES);
		assertConfigsContainClasses(singleList.getAllowedSecretClientConfigs(false, READ_OP), ONE_CLASSES);

		SecretClientConfigHolder separateLists = new SecretClientConfigHolder(ONE_CONFIG, TWO_CONFIG);
		assertConfigsContainClasses(separateLists.getAllowedSecretClientConfigs(true, READ_OP), ONE_CLASSES);
		assertConfigsContainClasses(separateLists.getAllowedSecretClientConfigs(false, READ_OP), TWO_CLASSES);
	}

	//NOTE: this is meant to test results of SecretClientConfigHolder#getAllowedSecretClientSupplierClasses, not the
	//possibilities of SecretClientConfig#allowed, which has its own tests in SecretClientConfigUnitTests
	@Test
	public void testAllowed(){
		SecretClientConfigHolder readWrite = new SecretClientConfigHolder(List.of(
				SecretClientConfig.readOnly("TEST", MemorySecretClientSupplier.class),
				SecretClientConfig.allOps("TEST", LocalStorageSecretClientSupplier.class)));

		assertConfigsContainClasses(readWrite.getAllowedSecretClientConfigs(true, READ_OP), TWO_CLASSES);
		assertConfigsContainClasses(readWrite.getAllowedSecretClientConfigs(true, WRITE_OP), List.of(
				LocalStorageSecretClientSupplier.class));


		SecretClientConfigHolder writeRead = new SecretClientConfigHolder(List.of(
				SecretClientConfig.allOps("TEST", MemorySecretClientSupplier.class),
				SecretClientConfig.readOnly("TEST", LocalStorageSecretClientSupplier.class)));
		assertConfigsContainClasses(writeRead.getAllowedSecretClientConfigs(true, READ_OP), TWO_CLASSES);
		assertConfigsContainClasses(writeRead.getAllowedSecretClientConfigs(true, WRITE_OP), List.of(
				MemorySecretClientSupplier.class));
	}

	private static SecretClientConfig makeConfig(Class<? extends SecretClientSupplier> cls){
		return SecretClientConfig.allOps("TEST", cls);
	}

	private static void assertConfigsContainClasses(Scanner<SecretClientConfig> secretClientConfigScanner,
			List<Class<? extends SecretClientSupplier>> expected){
		Assert.assertEquals(secretClientConfigScanner
				.map(SecretClientConfig::getSecretClientSupplierClass)
				.list(),
				expected);
	}

}
