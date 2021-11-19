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
package io.datarouter.secret.op;

import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.secret.op.SecretOpConfig.Namespace;

public class SecretOpConfigUnitTests{

	@Test
	public void testNamespaces(){
		var defaultAppNamespaceConfig = getBuilder().build();
		Assert.assertEquals(defaultAppNamespaceConfig.manualNamespace, "");
		Assert.assertEquals(defaultAppNamespaceConfig.namespaceType, Namespace.APP);

		var sharedNamespaceConfig = getBuilder()
				.useSharedNamespace()
				.build();
		Assert.assertEquals(sharedNamespaceConfig.manualNamespace, "");
		Assert.assertEquals(sharedNamespaceConfig.namespaceType, Namespace.SHARED);

		var manualNamespaceConfig = getBuilder()
				.useManualNamespace("manual")
				.build();
		Assert.assertEquals(manualNamespaceConfig.manualNamespace, "manual/");
		Assert.assertEquals(manualNamespaceConfig.namespaceType, Namespace.MANUAL);

		var manualSlashedNamespaceConfig = getBuilder()
				.useManualNamespace("manual/")
				.build();
		Assert.assertEquals(manualSlashedNamespaceConfig.manualNamespace, "manual/");
		Assert.assertEquals(manualSlashedNamespaceConfig.namespaceType, Namespace.MANUAL);

		Assert.assertThrows(() -> getBuilder()
				.useManualNamespace(null)
				.build());

		Assert.assertThrows(() -> getBuilder()
				.useManualNamespace("")
				.build());
	}

	@Test
	public void testTargetSecretClientConfig(){
		var noTargetConfig = getBuilder().build();
		Assert.assertEquals(noTargetConfig.targetSecretClientConfig, Optional.empty());

		var yesTargetConfig = getBuilder()
				.useTargetSecretClientConfig(Optional.of("config"))
				.build();
		Assert.assertEquals(yesTargetConfig.targetSecretClientConfig, Optional.of("config"));

		var emptyTargetConfig = getBuilder()
				.useTargetSecretClientConfig(Optional.empty())
				.build();
		Assert.assertEquals(emptyTargetConfig.targetSecretClientConfig, Optional.empty());

		Assert.assertThrows(() -> getBuilder()
				.useTargetSecretClientConfig(Optional.of(""))
				.build());

		Assert.assertThrows(() -> getBuilder()
				.useTargetSecretClientConfig(null)
				.build());
	}

	@Test
	public void testBooleans(){
		var defaultConfig = getBuilder().build();
		Assert.assertEquals(defaultConfig.shouldRecord, true);
		Assert.assertEquals(defaultConfig.shouldLog, true);
		Assert.assertEquals(defaultConfig.shouldSkipSerialization, false);
		Assert.assertEquals(defaultConfig.shouldApplyToAllClients, false);

		var setConfig = getBuilder()
				.disableRecording()
				.disableLogging()
				.disableSerialization()
				.applyToAllSuppliers()
				.build();
		Assert.assertEquals(setConfig.shouldRecord, false);
		Assert.assertEquals(setConfig.shouldLog, false);
		Assert.assertEquals(setConfig.shouldSkipSerialization, true);
		Assert.assertEquals(setConfig.shouldApplyToAllClients, true);
	}

	@Test
	public void testReason(){
		var reason = SecretOpReason.automatedOp("a reason");
		var config = SecretOpConfig.builder(reason).build();
		Assert.assertEquals(config.reason, reason);
	}

	private static SecretOpConfig.Builder getBuilder(){
		return SecretOpConfig.builder(SecretOpReason.automatedOp(SecretOpConfigUnitTests.class.getName()));
	}

}
