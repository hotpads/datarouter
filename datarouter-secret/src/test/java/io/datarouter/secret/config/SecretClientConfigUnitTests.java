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

import static org.mockito.Mockito.mock;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.scanner.Scanner;
import io.datarouter.secret.client.SecretClient.SecretClientSupplier;
import io.datarouter.secret.client.memory.MemorySecretClientSupplier;
import io.datarouter.secret.op.SecretOp;
import io.datarouter.secret.op.client.SecretClientOpType;

public class SecretClientConfigUnitTests{

	private static final Class<? extends SecretClientSupplier> SUPPLIER = MemorySecretClientSupplier.class;

	private static final Set<SecretClientOpType> WRITE_OPS = Set.of(
			SecretClientOpType.CREATE,
			SecretClientOpType.UPDATE,
			SecretClientOpType.PUT,
			SecretClientOpType.DELETE);
	private static final Set<SecretClientOpType> READ_OPS = Set.of(SecretClientOpType.READ, SecretClientOpType.LIST);
	private static final Set<SecretClientOpType> ALL_OPS = Scanner.concat(WRITE_OPS, READ_OPS).collect(HashSet::new);

	@Test
	public void testInitMethods(){
		Assert.assertThrows(IllegalArgumentException.class, () -> SecretClientSupplierConfig.allOps("TEST", null));
		Assert.assertThrows(IllegalArgumentException.class, () -> SecretClientSupplierConfig.readOnly("TEST", null));
		Assert.assertThrows(
				IllegalArgumentException.class,
				() -> SecretClientSupplierConfig.readOnlyWithNames("TEST", null, null));
		Assert.assertThrows(
				IllegalArgumentException.class,
				() -> SecretClientSupplierConfig.readOnlyWithNames("TEST", SUPPLIER, null));
		Assert.assertThrows(
				IllegalArgumentException.class,
				() -> SecretClientSupplierConfig.readOnlyWithNames("TEST", SUPPLIER, Set.of()));

		SecretClientSupplierConfig config = SecretClientSupplierConfig.allOps("TEST1", SUPPLIER);
		Assert.assertEquals(config.getConfigName(), "TEST1");
		Assert.assertEquals(config.getSecretClientSupplierClass(), SUPPLIER);
		config = SecretClientSupplierConfig.readOnly("TEST2", SUPPLIER);
		Assert.assertEquals(config.getConfigName(), "TEST2");
		Assert.assertEquals(config.getSecretClientSupplierClass(), SUPPLIER);
		config = SecretClientSupplierConfig.readOnlyWithNames("TEST3", SUPPLIER, Set.of("name"));
		Assert.assertEquals(config.getConfigName(), "TEST3");
		Assert.assertEquals(config.getSecretClientSupplierClass(), SUPPLIER);
	}

	@Test
	public void testAllowedTargetSecretClientConfig(){
		SecretClientSupplierConfig allConfig = SecretClientSupplierConfig.allOps("TEST", SUPPLIER);
		var noTarget = emptyOpInfo(SecretClientOpType.DELETE);
		Assert.assertTrue(allConfig.allowed(noTarget));

		var matchTarget = namedTargetedOpInfo(SecretClientOpType.DELETE, "", "TEST");
		Assert.assertTrue(allConfig.allowed(matchTarget));

		var noMatchTarget = namedTargetedOpInfo(SecretClientOpType.DELETE, "", "TEST2");
		Assert.assertFalse(allConfig.allowed(noMatchTarget));
	}

	@Test
	public void testAllowedOps(){
		SecretClientSupplierConfig allConfig = SecretClientSupplierConfig.allOps("TEST", SUPPLIER);
		Scanner.of(ALL_OPS)
				.map(SecretClientConfigUnitTests::emptyOpInfo)
				.forEach(opInfo -> Assert.assertTrue(allConfig.allowed(opInfo)));

		SecretClientSupplierConfig readOnlyConfig = SecretClientSupplierConfig.readOnly("TEST", SUPPLIER);
		Scanner.of(READ_OPS)
				.map(SecretClientConfigUnitTests::emptyOpInfo)
				.forEach(opInfo -> Assert.assertTrue(readOnlyConfig.allowed(opInfo)));
		Scanner.of(WRITE_OPS)
				.map(SecretClientConfigUnitTests::emptyOpInfo)
				.forEach(opInfo -> Assert.assertFalse(readOnlyConfig.allowed(opInfo)));
	}

	@Test
	public void testreadOnlyWithNames(){
		SecretClientSupplierConfig namedConfig = SecretClientSupplierConfig.readOnlyWithNames(
				"TEST",
				SUPPLIER,
				Set.of("allowed"));
		//test read/write with allowed name
		Scanner.of(SecretClientOpType.READ)
				.map(op -> namedOpInfo(op, "allowed"))
				.forEach(opInfo -> Assert.assertTrue(namedConfig.allowed(opInfo)));
		Scanner.of(SecretClientOpType.LIST)
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

	public static SecretOp<?,?,?,?> emptyOpInfo(SecretClientOpType opType){
		return namedOpInfo(opType, "");
	}

	private static SecretOp<?,?,?,?> namedOpInfo(SecretClientOpType opType, String name){
		return namedTargetedOpInfo(opType, name, null);
	}

	private static SecretOp<?,?,?,?> namedTargetedOpInfo(SecretClientOpType opType, String name,
			String targetSecretClientConfig){
		SecretOp<?,?,?,?> secretOp = mock(SecretOp.class);
		Mockito.when(secretOp.getName()).thenReturn(name);
		Mockito.when(secretOp.getTargetSecretClientConfig()).thenReturn(Optional.ofNullable(targetSecretClientConfig));
		Mockito.when(secretOp.getSecretClientOpType()).thenReturn(opType);
		return secretOp;
	}

}
