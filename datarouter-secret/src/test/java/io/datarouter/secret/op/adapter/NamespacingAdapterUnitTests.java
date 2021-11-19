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
package io.datarouter.secret.op.adapter;

import java.util.List;
import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.secret.client.Secret;
import io.datarouter.secret.op.SecretOpConfig;
import io.datarouter.secret.op.SecretOpReason;
import io.datarouter.secret.op.adapter.NamespacingAdapter.ListNamespacingAdapter;
import io.datarouter.secret.op.adapter.NamespacingAdapter.NamespacingMode;
import io.datarouter.secret.op.adapter.NamespacingAdapter.OptionalStringNamespacingAdapter;
import io.datarouter.secret.op.adapter.NamespacingAdapter.SecretNamespacingAdapter;
import io.datarouter.secret.op.adapter.NamespacingAdapter.StringNamespacingAdapter;
import io.datarouter.secret.service.SecretNamespacer;

public class NamespacingAdapterUnitTests{

	private static final SecretNamespacer namespacer = new SecretNamespacer.DevelopmentNamespacer();
	private static final SecretOpReason reason = SecretOpReason.automatedOp(SecretOpSerializationAdapterUnitTests.class
			.getName());
	private static final SecretOpConfig appConfig = SecretOpConfig.builder(reason).build();
	private static final SecretOpConfig sharedConfig = SecretOpConfig.builder(reason)
			.useSharedNamespace()
			.build();
	private static final SecretOpConfig manualConfig = SecretOpConfig.builder(reason)
			.useManualNamespace("manual")
			.build();

	@Test
	public void testStringNamespacingAdapter(){
		var test = "test";
		var addingApp = new StringNamespacingAdapter(namespacer, appConfig, NamespacingMode.ADDING);
		var addedApp = addingApp.adapt(test);
		Assert.assertEquals(addedApp, namespacer.appNamespaced(test));
		var addingShared = new StringNamespacingAdapter(namespacer, sharedConfig, NamespacingMode.ADDING);
		var addedShared = addingShared.adapt(test);
		Assert.assertEquals(addedShared, namespacer.sharedNamespaced(test));
		var addingManual = new StringNamespacingAdapter(namespacer, manualConfig, NamespacingMode.ADDING);
		var addedManual = addingManual.adapt(test);
		Assert.assertEquals(addedManual, "manual/test");

		var removingApp = new StringNamespacingAdapter(namespacer, appConfig, NamespacingMode.REMOVING);
		Assert.assertEquals(removingApp.adapt(addedApp), test);
		Assert.assertThrows(() -> removingApp.adapt(test));
		var removingShared = new StringNamespacingAdapter(namespacer, sharedConfig, NamespacingMode.REMOVING);
		Assert.assertEquals(removingShared.adapt(addedShared), test);
		Assert.assertThrows(() -> removingShared.adapt(test));
		var removingManual = new StringNamespacingAdapter(namespacer, manualConfig, NamespacingMode.REMOVING);
		Assert.assertEquals(removingManual.adapt(addedManual), test);
		Assert.assertThrows(() -> removingManual.adapt(test));
	}

	@Test
	public void testOptionalStringNamespacingAdapter(){
		String name = "test";
		var test = Optional.of(name);
		var addingApp = new OptionalStringNamespacingAdapter(namespacer, appConfig, NamespacingMode.ADDING);
		var addedApp = addingApp.adapt(test);
		Assert.assertEquals(addedApp, Optional.of(namespacer.appNamespaced(name)));
		var addingShared = new OptionalStringNamespacingAdapter(namespacer, sharedConfig, NamespacingMode.ADDING);
		var addedShared = addingShared.adapt(test);
		Assert.assertEquals(addedShared, Optional.of(namespacer.sharedNamespaced(name)));
		var addingManual = new OptionalStringNamespacingAdapter(namespacer, manualConfig, NamespacingMode.ADDING);
		var addedManual = addingManual.adapt(test);
		Assert.assertEquals(addedManual, Optional.of("manual/test"));

		var removingApp = new OptionalStringNamespacingAdapter(namespacer, appConfig, NamespacingMode.REMOVING);
		Assert.assertEquals(removingApp.adapt(addedApp), test);
		Assert.assertThrows(() -> removingApp.adapt(test));
		var removingShared = new OptionalStringNamespacingAdapter(namespacer, sharedConfig, NamespacingMode.REMOVING);
		Assert.assertEquals(removingShared.adapt(addedShared), test);
		Assert.assertThrows(() -> removingShared.adapt(test));
		var removingManual = new OptionalStringNamespacingAdapter(namespacer, manualConfig, NamespacingMode.REMOVING);
		Assert.assertEquals(removingManual.adapt(addedManual), test);
		Assert.assertThrows(() -> removingManual.adapt(test));
	}

	@Test
	public void testListNamespacingAdapter(){
		String name1 = "test";
		String name2 = "test2";
		var test = List.of(name1, name2);
		var addingApp = new ListNamespacingAdapter(namespacer, appConfig, NamespacingMode.ADDING);
		var addedApp = addingApp.adapt(test);
		Assert.assertEquals(addedApp, List.of(namespacer.appNamespaced(name1), namespacer.appNamespaced(name2)));
		var addingShared = new ListNamespacingAdapter(namespacer, sharedConfig, NamespacingMode.ADDING);
		var addedShared = addingShared.adapt(test);
		Assert.assertEquals(
				addedShared,
				List.of(namespacer.sharedNamespaced(name1), namespacer.sharedNamespaced(name2)));
		var addingManual = new ListNamespacingAdapter(namespacer, manualConfig, NamespacingMode.ADDING);
		var addedManual = addingManual.adapt(test);
		Assert.assertEquals(addedManual, List.of("manual/" + name1, "manual/" + name2));

		var removingApp = new ListNamespacingAdapter(namespacer, appConfig, NamespacingMode.REMOVING);
		Assert.assertEquals(removingApp.adapt(addedApp), test);
		Assert.assertThrows(() -> removingApp.adapt(test));
		var removingShared = new ListNamespacingAdapter(namespacer, sharedConfig, NamespacingMode.REMOVING);
		Assert.assertEquals(removingShared.adapt(addedShared), test);
		Assert.assertThrows(() -> removingShared.adapt(test));
		var removingManual = new ListNamespacingAdapter(namespacer, manualConfig, NamespacingMode.REMOVING);
		Assert.assertEquals(removingManual.adapt(addedManual), test);
		Assert.assertThrows(() -> removingManual.adapt(test));
	}

	@Test
	public void testSecretNamespacingAdapter(){
		String name = "test";
		var test = new Secret(name, "value");
		var addingApp = new SecretNamespacingAdapter(namespacer, appConfig, NamespacingMode.ADDING);
		var addedApp = addingApp.adapt(test);
		Assert.assertEquals(addedApp.getName(), namespacer.appNamespaced(name));
		var addingShared = new SecretNamespacingAdapter(namespacer, sharedConfig, NamespacingMode.ADDING);
		var addedShared = addingShared.adapt(test);
		Assert.assertEquals(addedShared.getName(), namespacer.sharedNamespaced(name));
		var addingManual = new SecretNamespacingAdapter(namespacer, manualConfig, NamespacingMode.ADDING);
		var addedManual = addingManual.adapt(test);
		Assert.assertEquals(addedManual.getName(), "manual/test");

		var removingApp = new SecretNamespacingAdapter(namespacer, appConfig, NamespacingMode.REMOVING);
		Assert.assertEquals(removingApp.adapt(addedApp).getName(), name);
		Assert.assertThrows(() -> removingApp.adapt(test));
		var removingShared = new SecretNamespacingAdapter(namespacer, sharedConfig, NamespacingMode.REMOVING);
		Assert.assertEquals(removingShared.adapt(addedShared).getName(), name);
		Assert.assertThrows(() -> removingShared.adapt(test));
		var removingManual = new SecretNamespacingAdapter(namespacer, manualConfig, NamespacingMode.REMOVING);
		Assert.assertEquals(removingManual.adapt(addedManual).getName(), name);
		Assert.assertThrows(() -> removingManual.adapt(test));
	}

}
