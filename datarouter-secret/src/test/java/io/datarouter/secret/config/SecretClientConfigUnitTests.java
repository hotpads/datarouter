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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.scanner.Scanner;
import io.datarouter.secret.client.SecretClient.SecretClientSupplier;
import io.datarouter.secret.client.memory.MemorySecretClientSupplier;
import io.datarouter.secret.op.SecretOpInfo;
import io.datarouter.secret.op.SecretOpReason;
import io.datarouter.secret.op.SecretOpType;

public class SecretClientConfigUnitTests{

	private static final Class<? extends SecretClientSupplier> SUPPLIER = MemorySecretClientSupplier.class;

	private static final Set<SecretOpType> WRITE_OPS = Set.of(SecretOpType.CREATE, SecretOpType.UPDATE, SecretOpType
			.PUT, SecretOpType.DELETE, SecretOpType.MIGRATE);
	private static final Set<SecretOpType> READ_OPS = Set.of(SecretOpType.READ, SecretOpType.LIST);
	private static final Set<SecretOpType> ALL_OPS = Scanner.concat(WRITE_OPS, READ_OPS).collect(HashSet::new);

	private static SecretOpReason REASON = SecretOpReason.automatedOp(SecretClientConfigUnitTests.class
			.getSimpleName());

	@Test
	public void testInitMethods(){
		Assert.assertThrows(IllegalArgumentException.class, () -> SecretClientConfig.allOps("TEST", null));
		Assert.assertThrows(IllegalArgumentException.class, () -> SecretClientConfig.readOnly("TEST", null));
		Assert.assertThrows(IllegalArgumentException.class, () -> SecretClientConfig.readOnlyWithNames("TEST", null,
				null));
		Assert.assertThrows(IllegalArgumentException.class, () -> SecretClientConfig.readOnlyWithNames("TEST", SUPPLIER,
				null));
		Assert.assertThrows(IllegalArgumentException.class, () -> SecretClientConfig.readOnlyWithNames("TEST", SUPPLIER,
				Set.of()));

		SecretClientConfig config = SecretClientConfig.allOps("TEST1", SUPPLIER);
		Assert.assertEquals(config.getConfigName(), "TEST1");
		Assert.assertEquals(config.getSecretClientSupplierClass(), SUPPLIER);
		config = SecretClientConfig.readOnly("TEST2", SUPPLIER);
		Assert.assertEquals(config.getConfigName(), "TEST2");
		Assert.assertEquals(config.getSecretClientSupplierClass(), SUPPLIER);
		config = SecretClientConfig.readOnlyWithNames("TEST3", SUPPLIER, Set.of("name"));
		Assert.assertEquals(config.getConfigName(), "TEST3");
		Assert.assertEquals(config.getSecretClientSupplierClass(), SUPPLIER);
	}

	@Test
	public void testAllowedTargetSecretClientConfig(){
		SecretClientConfig allConfig = SecretClientConfig.allOps("TEST", SUPPLIER);
		SecretOpInfo noTarget = emptyOpInfo(SecretOpType.DELETE);
		Assert.assertTrue(allConfig.allowed(noTarget));

		SecretOpInfo matchTarget = new SecretOpInfo(SecretOpType.DELETE, "", "", REASON, Optional.of("TEST"));
		Assert.assertTrue(allConfig.allowed(matchTarget));

		SecretOpInfo noMatchTarget = new SecretOpInfo(SecretOpType.DELETE, "", "", REASON, Optional.of("TEST2"));
		Assert.assertFalse(allConfig.allowed(noMatchTarget));
	}

	@Test
	public void testAllowedOps(){
		SecretClientConfig allConfig = SecretClientConfig.allOps("TEST", SUPPLIER);
		Scanner.of(ALL_OPS)
				.map(SecretClientConfigUnitTests::emptyOpInfo)
				.forEach(opInfo -> Assert.assertTrue(allConfig.allowed(opInfo)));

		SecretClientConfig readOnlyConfig = SecretClientConfig.readOnly("TEST", SUPPLIER);
		Scanner.of(READ_OPS)
				.map(SecretClientConfigUnitTests::emptyOpInfo)
				.forEach(opInfo -> Assert.assertTrue(readOnlyConfig.allowed(opInfo)));
		Scanner.of(WRITE_OPS)
				.map(SecretClientConfigUnitTests::emptyOpInfo)
				.forEach(opInfo -> Assert.assertFalse(readOnlyConfig.allowed(opInfo)));
	}

	@Test
	public void testreadOnlyWithNames(){
		SecretClientConfig namedConfig = SecretClientConfig.readOnlyWithNames("TEST", SUPPLIER, Set.of("allowed"));
		//test read/write with allowed name
		Scanner.of(SecretOpType.READ)
				.map(op -> namedOpInfo(op, "allowed"))
				.forEach(opInfo -> Assert.assertTrue(namedConfig.allowed(opInfo)));
		Scanner.of(SecretOpType.LIST)
				.map(op -> namedOpInfo(op, "allowed"))
				.forEach(opInfo -> Assert.assertFalse(namedConfig.allowed(opInfo)));
		Scanner.of(WRITE_OPS)
				.map(op -> namedOpInfo(op, "allowed"))
				.forEach(opInfo -> Assert.assertFalse(namedConfig.allowed(opInfo)));

		//test all ops with with name that isn't allowed
		Scanner.of(ALL_OPS)
				.map(op -> namedOpInfo(op, "not"))
				.forEach(opInfo -> Assert.assertFalse(namedConfig.allowed(opInfo)));
	}

	private static SecretOpInfo emptyOpInfo(SecretOpType op){
		return namedOpInfo(op, "");
	}

	private static SecretOpInfo namedOpInfo(SecretOpType op, String name){
		return new SecretOpInfo(op, "", name, REASON);
	}

}
