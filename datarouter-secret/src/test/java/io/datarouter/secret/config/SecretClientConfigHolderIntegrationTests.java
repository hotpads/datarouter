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
package io.datarouter.secret.config;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.scanner.Scanner;
import io.datarouter.secret.client.SecretClientSupplier;
import io.datarouter.secret.client.local.LocalStorageSecretClientSupplier;
import io.datarouter.secret.client.memory.MemorySecretClientSupplier;
import io.datarouter.secret.op.SecretOp;
import io.datarouter.secret.op.SecretOpInfo;
import io.datarouter.secret.op.SecretOpReason;

public class SecretClientConfigHolderIntegrationTests{

	private static final SecretOpInfo READ_OP = new SecretOpInfo(SecretOp.READ, "", "", SecretOpReason.automatedOp(
			SecretClientConfigHolderIntegrationTests.class.getSimpleName()));
	private static final SecretOpInfo WRITE_OP = new SecretOpInfo(SecretOp.CREATE, "", "", SecretOpReason.automatedOp(
			SecretClientConfigHolderIntegrationTests.class.getSimpleName()));

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
		Assert.assertEquals(empty.getAllowedSecretClientSupplierClasses(true, READ_OP).list(), List.of());
		Assert.assertEquals(empty.getAllowedSecretClientSupplierClasses(false, READ_OP).list(), List.of());

		SecretClientConfigHolder singleList = new SecretClientConfigHolder(ONE_CONFIG);
		Assert.assertEquals(singleList.getAllowedSecretClientSupplierClasses(true, READ_OP).list(), ONE_CLASSES);
		Assert.assertEquals(singleList.getAllowedSecretClientSupplierClasses(false, READ_OP).list(), ONE_CLASSES);

		SecretClientConfigHolder separateLists = new SecretClientConfigHolder(ONE_CONFIG, TWO_CONFIG);
		Assert.assertEquals(separateLists.getAllowedSecretClientSupplierClasses(true, READ_OP).list(), ONE_CLASSES);
		Assert.assertEquals(separateLists.getAllowedSecretClientSupplierClasses(false, READ_OP).list(), TWO_CLASSES);
	}

	//NOTE: this is meant to test results of SecretClientConfigHolder#getAllowedSecretClientSupplierClasses, not the
	//possibilities of SecretClientConfig#allowed, which has its own tests in SecretClientConfigUnitTests
	@Test
	public void testAllowed(){
		SecretClientConfigHolder readWrite = new SecretClientConfigHolder(List.of(
				SecretClientConfig.readOnly(MemorySecretClientSupplier.class),
				SecretClientConfig.allOps(LocalStorageSecretClientSupplier.class)));

		Assert.assertEquals(readWrite.getAllowedSecretClientSupplierClasses(true, READ_OP).list(), TWO_CLASSES);
		Assert.assertEquals(readWrite.getAllowedSecretClientSupplierClasses(true, WRITE_OP).list(), List.of(
				LocalStorageSecretClientSupplier.class));


		SecretClientConfigHolder writeRead = new SecretClientConfigHolder(List.of(
				SecretClientConfig.allOps(MemorySecretClientSupplier.class),
				SecretClientConfig.readOnly(LocalStorageSecretClientSupplier.class)));
		Assert.assertEquals(writeRead.getAllowedSecretClientSupplierClasses(true, READ_OP).list(), TWO_CLASSES);
		Assert.assertEquals(writeRead.getAllowedSecretClientSupplierClasses(true, WRITE_OP).list(), List.of(
				MemorySecretClientSupplier.class));
	}

	private static SecretClientConfig makeConfig(Class<? extends SecretClientSupplier> cls){
		return SecretClientConfig.allOps(cls);
	}

}
