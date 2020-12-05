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

import java.util.HashSet;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.scanner.Scanner;
import io.datarouter.secret.client.SecretClientSupplier;
import io.datarouter.secret.client.memory.MemorySecretClientSupplier;
import io.datarouter.secret.op.SecretOp;
import io.datarouter.secret.op.SecretOpInfo;
import io.datarouter.secret.op.SecretOpReason;

public class SecretClientConfigUnitTests{

	private static final Class<? extends SecretClientSupplier> SUPPLIER = MemorySecretClientSupplier.class;

	private static final Set<SecretOp> WRITE_OPS = Set.of(SecretOp.CREATE, SecretOp.UPDATE, SecretOp.PUT, SecretOp
			.DELETE, SecretOp.MIGRATE);
	private static final Set<SecretOp> READ_OPS = Set.of(SecretOp.READ, SecretOp.LIST);
	private static final Set<SecretOp> ALL_OPS = Scanner.concat(WRITE_OPS, READ_OPS).collect(HashSet::new);

	private static SecretOpReason REASON = SecretOpReason.automatedOp(SecretClientConfigUnitTests.class
			.getSimpleName());

	@Test
	public void testInitMethods(){
		Assert.assertThrows(IllegalArgumentException.class, () -> SecretClientConfig.allOps(null));
		Assert.assertThrows(IllegalArgumentException.class, () -> SecretClientConfig.readOnly(null));
		Assert.assertThrows(IllegalArgumentException.class, () -> SecretClientConfig.readOnlyWithNames(null, null));
		Assert.assertThrows(IllegalArgumentException.class, () -> SecretClientConfig.readOnlyWithNames(SUPPLIER, null));
		Assert.assertThrows(IllegalArgumentException.class, () -> SecretClientConfig.readOnlyWithNames(SUPPLIER, Set
				.of()));

		SecretClientConfig config = SecretClientConfig.allOps(SUPPLIER);
		Assert.assertEquals(config.getSecretClientSupplierClass(), SUPPLIER);
		SecretClientConfig.readOnly(SUPPLIER);
		Assert.assertEquals(config.getSecretClientSupplierClass(), SUPPLIER);
		SecretClientConfig.readOnlyWithNames(SUPPLIER, Set.of("name"));
		Assert.assertEquals(config.getSecretClientSupplierClass(), SUPPLIER);
	}

	@Test
	public void testAllowedOps(){
		SecretClientConfig allConfig = SecretClientConfig.allOps(SUPPLIER);
		Scanner.of(ALL_OPS)
				.map(SecretClientConfigUnitTests::emptyOpInfo)
				.forEach(opInfo -> Assert.assertTrue(allConfig.allowed(opInfo)));

		SecretClientConfig readOnlyConfig = SecretClientConfig.readOnly(SUPPLIER);
		Scanner.of(READ_OPS)
				.map(SecretClientConfigUnitTests::emptyOpInfo)
				.forEach(opInfo -> Assert.assertTrue(readOnlyConfig.allowed(opInfo)));
		Scanner.of(WRITE_OPS)
				.map(SecretClientConfigUnitTests::emptyOpInfo)
				.forEach(opInfo -> Assert.assertFalse(readOnlyConfig.allowed(opInfo)));
	}

	@Test
	public void testreadOnlyWithNames(){
		SecretClientConfig namedConfig = SecretClientConfig.readOnlyWithNames(SUPPLIER, Set.of("allowed"));
		//test read/write with allowed name
		Scanner.of(SecretOp.READ)
				.map(op -> namedOpInfo(op, "allowed"))
				.forEach(opInfo -> Assert.assertTrue(namedConfig.allowed(opInfo)));
		Scanner.of(SecretOp.LIST)
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

	private static SecretOpInfo emptyOpInfo(SecretOp op){
		return namedOpInfo(op, "");
	}

	private static SecretOpInfo namedOpInfo(SecretOp op, String name){
		return new SecretOpInfo(op, "", name, REASON);
	}

}
